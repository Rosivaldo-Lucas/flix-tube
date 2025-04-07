package br.com.rosivaldolucas.flixtube.ms_video_admin.messaging;

import br.com.rosivaldolucas.flixtube.ms_video_admin.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class VideoUploadedProducer {

    private final RabbitTemplate rabbitTemplate;

    public void send(VideoUploadedEvent event) {
        this.rabbitTemplate.convertAndSend(
                RabbitMQConfig.FLIXTUBE_DIRECT_EXCHANGE_NAME,
                RabbitMQConfig.VIDEO_ENCODER_UPLOADED_ROUTING_KEY,
                event
        );
    }

}
