package br.com.rosivaldolucas.flixtube.ms_video_admin.messaging;

import br.com.rosivaldolucas.flixtube.ms_video_admin.messaging.dto.VideoProcessedEvent;
import br.com.rosivaldolucas.flixtube.ms_video_admin.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class VideoProcessedConsumer {

    private final VideoService videoService;

    @RabbitListener
    public void videoProcessed(VideoProcessedEvent event) {
        this.videoService.updateVideoAfterProcessing(event);
    }

}
