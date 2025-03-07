package br.com.rosivaldolucas.flixtube.ms_video_encoder.service;

import br.com.rosivaldolucas.flixtube.ms_video_encoder.cloud.dto.DownloadResponseDTO;
import br.com.rosivaldolucas.flixtube.ms_video_encoder.entity.Video;
import br.com.rosivaldolucas.flixtube.ms_video_encoder.messaging.dto.VideoUploadedEventDTO;
import br.com.rosivaldolucas.flixtube.ms_video_encoder.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
            DownloadResponseDTO downloadResponseDTO = this.videoProcessingService.download(this.BUCKET, key);

            this.updateStatus(video, "PERSISTING");
            this.persistInLocal(pathDir, filenameMp4, downloadResponseDTO.contentAsInputStream());

            this.updateStatus(video, "FRAGMENTING");
            this.videoProcessingService.fragment(filePathMp4, filePathFrag);

            this.updateStatus(video, "ENCODING");
            this.videoProcessingService.encode(filePathFrag, pathDir);

            this.updateStatus(video, "UPLOADING");
            this.videoProcessingService.upload(pathDir, video.getOutputFilePath());
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

    private void persistInLocal(String pathDir, String filename, InputStream content) {
        Path path = Path.of(pathDir);

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException();
        }

        Path filePath = path.resolve(filename);

        try (OutputStream os = Files.newOutputStream(filePath)) {
            os.write(content.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
