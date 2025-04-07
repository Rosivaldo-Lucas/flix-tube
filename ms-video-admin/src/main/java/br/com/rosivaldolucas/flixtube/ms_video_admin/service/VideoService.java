package br.com.rosivaldolucas.flixtube.ms_video_admin.service;

import br.com.rosivaldolucas.flixtube.ms_video_admin.cloud.S3Integration;
import br.com.rosivaldolucas.flixtube.ms_video_admin.controller.dto.CreateVideoRequest;
import br.com.rosivaldolucas.flixtube.ms_video_admin.entity.Video;
import br.com.rosivaldolucas.flixtube.ms_video_admin.messaging.VideoUploadedEvent;
import br.com.rosivaldolucas.flixtube.ms_video_admin.messaging.VideoUploadedProducer;
import br.com.rosivaldolucas.flixtube.ms_video_admin.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@RequiredArgsConstructor
@Service
public class VideoService {

    private String BUCKET = "videoencoder";
    private String UPLOAD_PATH = "uploaded-video";

    private final VideoRepository videoRepository;
    private final S3Integration s3Integration;
    private final VideoUploadedProducer videoUploadedProducer;

    public void create(CreateVideoRequest createVideoRequest) {
        Video video = Video.create(
                createVideoRequest.getTitle(),
                createVideoRequest.getDescription(),
                createVideoRequest.getDuration(),
                createVideoRequest.getResource().getFilename(),
                this.UPLOAD_PATH
        );

        video = this.videoRepository.save(video);

        String key = String.format("%s/%s.mp4", this.UPLOAD_PATH, video.getId());

        InputStream content = new ByteArrayInputStream(createVideoRequest.getResource().getContent());

        this.s3Integration.uploadFile(this.BUCKET, key, content);

        VideoUploadedEvent event = VideoUploadedEvent
                .builder()
                .resourceId(String.format("video-encoder_%s", video.getId()))
                .inputFilename(String.format("%s.mp4", video.getId()))
                .build();

        this.videoUploadedProducer.send(event);
    }

}
