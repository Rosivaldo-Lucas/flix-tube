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

    private final VideoRepository videoRepository;
    private final VideoProcessingService videoProcessingService;
    private final S3Integration s3Integration;
    private final FileService fileService;

    public void startProcessing(VideoUploadedEventDTO eventDTO) {
        Video video = this.createAndSaveVideo(eventDTO);

        try {
            String pathDir = String.format("%s/%s", this.TMP_DIR, video.getId());
            String filenameMp4 = String.format("%s%s", video.getId(), VIDEO_MP4_EXTENSION);
            String filenameFrag = String.format("%s%s", video.getId(), VIDEO_FRAG_EXTENSION);
            String filePathMp4 = String.format("%s/%s", pathDir, filenameMp4);
            String filePathFrag = String.format("%s/%s", pathDir, filenameFrag);
            String key = String.format("%s/%s", video.getInputFilePath(), video.getInputFilename());

            this.updateStatus(video, "DOWNLOADING");
            DownloadResponseDTO downloadResponseDTO = this.s3Integration.downloadFile(this.BUCKET, key);

            this.updateStatus(video, "PERSISTING");
            this.fileService.persistFile(pathDir, filenameMp4, downloadResponseDTO.contentAsInputStream());

            this.updateStatus(video, "FRAGMENTING");
            this.videoProcessingService.fragment(filePathMp4, filePathFrag);

            this.updateStatus(video, "ENCODING");
            this.videoProcessingService.encode(filePathFrag, pathDir);

            this.updateStatus(video, "UPLOADING");
            this.processUpload(pathDir, video);

            this.fileService.cleanDir(pathDir);

            this.updateStatus(video, "COMPLETED");
        } catch (Exception ex) {
            this.updateStatus(video, "FAILED");
        }
    }

    private Video createAndSaveVideo(VideoUploadedEventDTO eventDTO) {
        Video video = new Video(
                eventDTO.resourceId(), this.BUCKET, this.INPUT_FILE_PATH,
                eventDTO.inputFilename(), this.OUTPUT_FILE_PATH
        );

        return this.videoRepository.save(video);
    }

    private void updateStatus(Video video, String status) {
        video.updateStatus(status);
        this.videoRepository.save(video);
    }

    private void processUpload(String pathDir, Video video) {
        String pathEncodedVideo = String.format("%s/video/avc1", pathDir);

        log.info("start process upload the files in path: {}", pathEncodedVideo);

        List<File> files = this.fileService.loadFiles(pathEncodedVideo);

        for (File file : files) {
            try (InputStream content = Files.newInputStream(Path.of(file.toURI()))) {
                log.info("uploading file '{}' to '{}'", file.getName(), this.BUCKET + "/" + video.getOutputFilePath());

                String keyOutput = String.format("%s/%s", video.getOutputFilePath(), file.getName());

                this.s3Integration.uploadFile(this.BUCKET, keyOutput, content);
            } catch (IOException ex) {
                log.error("error uploading file '{}'", file.getName(), ex);
                throw new RuntimeException(ex);
            }
        }

        log.info("end process upload the files in path: {}", pathEncodedVideo);
    }

}
