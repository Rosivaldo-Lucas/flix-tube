package br.com.rosivaldolucas.flixtube.ms_video_encoder.service;

import br.com.rosivaldolucas.flixtube.ms_video_encoder.entity.Video;
import br.com.rosivaldolucas.flixtube.ms_video_encoder.messaging.dto.VideoUploadedEventDTO;
import br.com.rosivaldolucas.flixtube.ms_video_encoder.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoProcessingManager {

    @Value("${ms-video-encoder.bucket}")
    private String BUCKET;

    @Value("${ms-video-encoder.input-file-path}")
    private String INPUT_FILE_PATH;

    @Value("${ms-video-encoder.output-file-path}")
    private String OUTPUT_FILE_PATH;

    private final VideoRepository videoRepository;
    private final VideoProcessingService videoProcessingService;

    public void startProcessing(VideoUploadedEventDTO eventDTO) {
        Video video = new Video(
                eventDTO.resourceId(), this.BUCKET, this.INPUT_FILE_PATH,
                eventDTO.inputFilename(), this.OUTPUT_FILE_PATH
        );

        this.videoRepository.save(video);

        try {
            this.download(video);

            // TODO: armazenar o video localmente

            this.fragment(video);

            this.encode(video);

            // TODO: upload do video encodado
        } catch (Exception ex) {
            video.updateStatus("FAILED");
            this.videoRepository.save(video);
        }
    }

    private void download(Video video) {
        video.updateStatus("DOWNLOADING");
        this.videoProcessingService.downloadVideo(video);
    }

    private void fragment(Video video) {
        video.updateStatus("FRAGMENTING");
        this.videoProcessingService.fragmentVideo(video);
    }

    private void encode(Video video) {
        video.updateStatus("ENCODING");
        this.videoProcessingService.encodeVideo(video);
    }

}
