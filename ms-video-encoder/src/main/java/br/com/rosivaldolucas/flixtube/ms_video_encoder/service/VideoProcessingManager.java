package br.com.rosivaldolucas.flixtube.ms_video_encoder.service;

import br.com.rosivaldolucas.flixtube.ms_video_encoder.cloud.S3Integration;
import br.com.rosivaldolucas.flixtube.ms_video_encoder.cloud.dto.DownloadResponseDTO;
import br.com.rosivaldolucas.flixtube.ms_video_encoder.entity.Video;
import br.com.rosivaldolucas.flixtube.ms_video_encoder.messaging.dto.VideoUploadedEventDTO;
import br.com.rosivaldolucas.flixtube.ms_video_encoder.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoProcessingManager {

    private static final String VIDEO_MP4_EXTENSION = ".mp4";
    private static final String VIDEO_FRAG_EXTENSION = ".frag";

    @Value("${ms-video-encoder.tmp-dir}")
    private String TMP_DIR;

    @Value("${ms-video-encoder.bucket}")
    private String BUCKET;

    @Value("${ms-video-encoder.input-path}")
    private String INPUT_FILE_PATH;

    @Value("${ms-video-encoder.output-path}")
    private String OUTPUT_FILE_PATH;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    private final VideoRepository videoRepository;
    private final VideoProcessingService videoProcessingService;
    private final S3Integration s3Integration;
    private final FileService fileService;

    public void startProcessing(VideoUploadedEventDTO eventDTO) {
        log.info("starting processing video: resoourceId={}, inputFilename={}", eventDTO.resourceId(), eventDTO.inputFilename());
        Video video = this.createAndSaveVideo(eventDTO);

        try {
            String pathDir = String.format("%s/%s", this.TMP_DIR, video.getId());
            String filenameMp4 = String.format("%s%s", video.getId(), VIDEO_MP4_EXTENSION);
            String filenameFrag = String.format("%s%s", video.getId(), VIDEO_FRAG_EXTENSION);
            String filePathMp4 = String.format("%s/%s", pathDir, filenameMp4);
            String filePathFrag = String.format("%s/%s", pathDir, filenameFrag);
            String key = String.format("%s/%s", video.getInputFilePath(), video.getInputFilename());

            log.info("processing video - pathDir={}, filenameMp4={}, filenameFrag={}, filePathMp4={}, filePathFrag={}", pathDir, filenameMp4, filenameFrag, filePathMp4, filePathFrag);

            log.info("start downloading file from S3 - bucket={}, key={}", this.BUCKET, key);
            this.updateStatus(video, "DOWNLOADING");
            DownloadResponseDTO downloadResponseDTO = this.s3Integration.downloadFile(this.BUCKET, key);
            log.info("end downloading file from S3");

            log.info("start persisting file to path: {}/{}", pathDir, filenameMp4);
            this.updateStatus(video, "PERSISTING");
            this.fileService.persistFile(pathDir, filenameMp4, downloadResponseDTO.contentAsInputStream());
            log.info("end persisting file");

            log.info("start fragmentation video");
            this.updateStatus(video, "FRAGMENTING");
            this.videoProcessingService.fragment(filePathMp4, filePathFrag);
            log.info("end fragmentation video");

            log.info("start encoding video");
            this.updateStatus(video, "ENCODING");
            this.videoProcessingService.encode(filePathFrag, pathDir);
            log.info("end encoding video");

            log.info("start process upload the files");
            this.updateStatus(video, "UPLOADING");
            boolean isSyncProcessingEnabled = eventDTO.isSyncProcessingEnabled() != null ? eventDTO.isSyncProcessingEnabled() : false;
            this.processUpload(pathDir, video, isSyncProcessingEnabled);
            log.info("end process upload the files");

            log.info("start cleaning directory");
            this.fileService.cleanDir(pathDir);
            log.info("end cleaning directory");

            this.updateStatus(video, "COMPLETED");
            log.info("end processing video");
        } catch (Exception ex) {
            log.error("error processing video with id={}", video.getId(), ex);
            video.addError(ex.getMessage());
            this.updateStatus(video, "FAILED");
        }
    }

    private Video createAndSaveVideo(VideoUploadedEventDTO eventDTO) {
        log.info("starting creating and saving video");

        Video video = new Video(
                eventDTO.resourceId(), this.BUCKET, this.INPUT_FILE_PATH,
                eventDTO.inputFilename(), this.OUTPUT_FILE_PATH
        );

        video = this.videoRepository.save(video);

        log.info("end creating and saving video with id={}", video.getId());
        return video;
    }

    private void updateStatus(Video video, String status) {
        log.info("starting updating video status to: {}", status);
        video.updateStatus(status);
        this.videoRepository.save(video);
        log.info("end updating video status to: {}", status);
    }

    private void processUpload(String pathDir, Video video, boolean isSyncProcessingEnabled) {
        log.info("starting process upload the files in mode: {}", (isSyncProcessingEnabled ? "sync" : "async"));
        long startTime = System.currentTimeMillis();

        String pathEncodedVideo = String.format("%s/video/avc1", pathDir);
        log.info("start process upload the files in path: {}", pathEncodedVideo);

        List<File> files = this.fileService.loadFiles(pathEncodedVideo);

        if (isSyncProcessingEnabled) {
            log.info("start process upload the files: sync");
            for (File file : files) {
                this.uploadFile(file, video.getOutputFilePath());
            }
            log.info("end process upload the files: sync");
        } else {
            log.info("start process upload the files: async");

            List<CompletableFuture<Void>> futures = files
                    .stream()
                    .map(file ->
                            CompletableFuture.runAsync(() -> this.uploadFile(file, video.getOutputFilePath()), this.executorService))
                    .toList();

            CompletableFuture<Void> allUploads = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            try {
                allUploads.get();
            } catch (Exception ex) {
                log.error("error waiting for uploads to complete", ex);
            }

            log.info("end process upload the files: async");
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("end process upload the files in path: {}. Time taken: {} ms", pathEncodedVideo, duration);
    }

    private void uploadFile(File file, String outputPath) {
        try (InputStream content = Files.newInputStream(Path.of(file.toURI()))) {
            String keyOutput = String.format("%s/%s", outputPath, file.getName());

            this.s3Integration.uploadFile(this.BUCKET, keyOutput, content);
        } catch (IOException ex) {
            log.error("error uploading file '{}'", file.getName(), ex);
            throw new RuntimeException("error uploading file '" + file.getName() + "'", ex);
        }
    }

}
