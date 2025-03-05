package br.com.rosivaldolucas.flixtube.ms_video_encoder.messaging;

import br.com.rosivaldolucas.flixtube.ms_video_encoder.config.RabbitMQConfig;
import br.com.rosivaldolucas.flixtube.ms_video_encoder.messaging.dto.VideoUploadedEventDTO;
import br.com.rosivaldolucas.flixtube.ms_video_encoder.service.VideoProcessingManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class VideoUploadedConsumer {

    private final VideoProcessingManager videoProcessingManager;

    @RabbitListener(queues = RabbitMQConfig.VIDEO_ENCODER_UPLOADED_QUEUE_NAME)
    public void onVideoUploaded(VideoUploadedEventDTO eventDTO) {
        this.videoProcessingManager.startProcessing(eventDTO);
    }

}
