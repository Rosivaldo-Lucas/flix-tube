package br.com.rosivaldolucas.flixtube.ms_video_admin.controller;

import br.com.rosivaldolucas.flixtube.ms_video_admin.controller.dto.CreateVideoRequest;
import br.com.rosivaldolucas.flixtube.ms_video_admin.controller.dto.CreateVideoResponse;
import br.com.rosivaldolucas.flixtube.ms_video_admin.entity.Video;
import br.com.rosivaldolucas.flixtube.ms_video_admin.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/videos")
public class VideoController {

    private final VideoService videoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateVideoResponse> create(@RequestPart("data") @Valid CreateVideoRequest createVideoRequest, @RequestPart("file") MultipartFile videoFile) {
        this.addResourceInRequest(createVideoRequest, videoFile);

        Video video = this.videoService.create(createVideoRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateVideoResponse(video.getId()));
    }

    private void addResourceInRequest(CreateVideoRequest createVideoRequest, MultipartFile videoFile) {
        if (videoFile == null) {
            throw new IllegalArgumentException("The video file cannot be null");
        }

        try {
            String filename = videoFile.getOriginalFilename();
            byte[] content = videoFile.getBytes();
            String formt = videoFile.getContentType();

            CreateVideoRequest.Resource resource = new CreateVideoRequest.Resource(filename, content, formt);
            createVideoRequest.setResource(resource);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
