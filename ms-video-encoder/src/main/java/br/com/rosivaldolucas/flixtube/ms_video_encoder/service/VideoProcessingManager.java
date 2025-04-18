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

    @Value("${flixtube.tmp-dir}")
    private String TMP_DIR;

    @Value("${flixtube.bucket}")
    private String BUCKET;

    @Value("${flixtube.upload-path}")
    private String UPLOAD_PATH;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    private final VideoRepository videoRepository;
    private final VideoProcessingService videoProcessingService;
    private final S3Integration s3Integration;
    private final FileService fileService;

    public void startProcessing(VideoUploadedEventDTO eventDTO) {
        Video video = this.createAndSaveVideo(eventDTO);

        String pathDir = String.format("%s/%s", this.TMP_DIR, video.getTransactionId());
        String filenameMp4 = String.format("%s%s", video.getTransactionId(), VIDEO_MP4_EXTENSION);
        String filenameFrag = String.format("%s%s", video.getTransactionId(), VIDEO_FRAG_EXTENSION);
        String filePathMp4 = String.format("%s/%s", pathDir, filenameMp4);
        String filePathFrag = String.format("%s/%s", pathDir, filenameFrag);
        String key = String.format("%s/%s", video.getInputPath(), video.getInputFilename());

        try {
            log.info("Downloading video with transactionId = '{}'", video.getTransactionId());
            this.updateStatus(video, "DOWNLOADING");
            DownloadResponseDTO downloadResponseDTO = this.s3Integration.downloadFile(this.BUCKET, key);

            log.info("Persisting locally video with transactionId = '{}'", video.getTransactionId());
            this.updateStatus(video, "PERSISTING");
            this.fileService.persistFile(pathDir, filenameMp4, downloadResponseDTO.contentAsInputStream());

            log.info("Fragmenting video with transactionId = '{}'", video.getTransactionId());
            this.updateStatus(video, "FRAGMENTING");
            this.videoProcessingService.fragment(filePathMp4, filePathFrag);

            log.info("Encoding video with transactionId = '{}'", video.getTransactionId());
            this.updateStatus(video, "ENCODING");
            this.videoProcessingService.encode(filePathFrag, pathDir);

            log.info("Uploading video with transactionId = '{}'", video.getTransactionId());
            this.updateStatus(video, "UPLOADING");
            this.processUpload(pathDir, video, false);

            this.fileService.cleanDir(pathDir);

            log.info("Completed video with transactionId = '{}'", video.getTransactionId());
            this.updateStatus(video, "COMPLETED");
        } catch (Exception ex) {
            video.addError(ex.getMessage());
            this.updateStatus(video, "FAILED");

            this.fileService.cleanDir(pathDir);

            log.error("Error processing video with transactionId = '{}'", video.getTransactionId(), ex);
            throw new RuntimeException("Error processing video with transactionId = " + video.getTransactionId(), ex);
        }
    }

    private Video createAndSaveVideo(VideoUploadedEventDTO eventDTO) {
        String outputPath = String.format("%s/$%s", this.UPLOAD_PATH, eventDTO.transactionId());

        Video video = new Video(
                eventDTO.transactionId(), this.BUCKET, this.UPLOAD_PATH,
                outputPath, eventDTO.filename()
        );

        video = this.videoRepository.save(video);

        log.info("Saving video with transactionId = '{}'", video.getTransactionId());

        return video;
    }

    private void updateStatus(Video video, String status) {
        video.updateStatus(status);
        this.videoRepository.save(video);
    }

    private void processUpload(String pathDir, Video video, boolean isSyncProcessingEnabled) {
        long startTime = System.currentTimeMillis();

        String pathEncodedVideo = String.format("%s/video/avc1", pathDir);

        List<File> files = this.fileService.loadFiles(pathEncodedVideo);

        log.info("Upload video to path = '{}'", video.getOutputPath());
        if (isSyncProcessingEnabled) {
            log.info("Start sync processing upload video fragments with transactionId = '{}'", video.getTransactionId());
            for (File file : files) {
                this.uploadFile(file, video.getOutputPath());
            }
        } else {
            log.info("Start async processing upload video fragments with transactionId = '{}'", video.getTransactionId());
            List<CompletableFuture<Void>> futures = files
                    .stream()
                    .map(file ->
                            CompletableFuture.runAsync(() -> this.uploadFile(file, video.getOutputPath()), this.executorService))
                    .toList();

            CompletableFuture<Void> allUploads = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

            try {
                allUploads.get();
            } catch (Exception ex) {
                log.error("Error processing upload video with transactionId = '{}'", video.getTransactionId(), ex);
            }
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("Upload completed in {} ms", duration);
    }

    private void uploadFile(File file, String outputPath) {
        try (InputStream content = Files.newInputStream(Path.of(file.toURI()))) {
            String keyOutput = String.format("%s/%s", outputPath, file.getName());

            this.s3Integration.uploadFile(this.BUCKET, keyOutput, content);
        } catch (IOException ex) {
            log.error("Error uploading file '{}'", file.getName(), ex);
            throw new RuntimeException("Error uploading file '" + file.getName() + "'", ex);
        }
    }

}
