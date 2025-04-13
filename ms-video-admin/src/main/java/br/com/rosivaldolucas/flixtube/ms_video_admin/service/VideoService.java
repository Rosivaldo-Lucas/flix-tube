package br.com.rosivaldolucas.flixtube.ms_video_admin.service;

import br.com.rosivaldolucas.flixtube.ms_video_admin.cloud.S3Integration;
import br.com.rosivaldolucas.flixtube.ms_video_admin.controller.dto.CreateVideoRequest;
import br.com.rosivaldolucas.flixtube.ms_video_admin.entity.Video;
import br.com.rosivaldolucas.flixtube.ms_video_admin.messaging.VideoUploadedEvent;
import br.com.rosivaldolucas.flixtube.ms_video_admin.messaging.VideoUploadedProducer;
import br.com.rosivaldolucas.flixtube.ms_video_admin.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoService {

    @Value("${flixtube.bucket}")
    private String BUCKET;

    @Value("${flixtube.upload-path}")
    private String UPLOAD_PATH;

    private final VideoRepository videoRepository;
    private final S3Integration s3Integration;
    private final VideoUploadedProducer videoUploadedProducer;

    public void create(CreateVideoRequest createVideoRequest) {
        Video video = Video.create(
                createVideoRequest.getTitle(),
                createVideoRequest.getDescription(),
                createVideoRequest.getDuration(),
                createVideoRequest.getResource().getFilename(),
                String.format("%s/%s", this.BUCKET, this.UPLOAD_PATH)
        );

        video = this.videoRepository.save(video);

        log.info("Transaction Id: {} - Created video with data: {}", video.getId(), video);

        this.uploadVideo(video, createVideoRequest.getResource().getContent());
        this.sendVideoUploadedEvent(video);
    }

    private void uploadVideo(Video video, byte[] content) {
        String key = String.format("%s/%s/%s.mp4", this.UPLOAD_PATH, video.getId(), video.getFilename());

        InputStream contentInputStream = new ByteArrayInputStream(content);

        this.s3Integration.uploadFile(this.BUCKET, key, contentInputStream);
    }

    private void sendVideoUploadedEvent(Video video) {
        String transactionId = video.getId();
        String inputPath = String.format("%s/%s", this.UPLOAD_PATH, video.getId());
        String filename = String.format("%s.mp4", video.getFilename());

        VideoUploadedEvent event = new VideoUploadedEvent(transactionId, inputPath, filename);

        this.videoUploadedProducer.send(event);
    }

}
