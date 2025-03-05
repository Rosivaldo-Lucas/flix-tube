package br.com.rosivaldolucas.flixtube.ms_video_encoder.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String VIDEO_ENCODER_UPLOADED_DIRECT_EXCHANGE_NAME = "video-encoder-direct-exchange";
    public static final String VIDEO_ENCODER_UPLOADED_QUEUE_NAME = "video-encoder-uploaded-queue";

    @Bean
    public Queue queueVideoEncoderUploaded() {
        return new Queue(VIDEO_ENCODER_UPLOADED_QUEUE_NAME, true);
    }

    @Bean
    public Binding bindingVideoEncoderUploadedQueue() {
        Queue videoUploadedQueue = new Queue(VIDEO_ENCODER_UPLOADED_QUEUE_NAME);
        DirectExchange videoUploadtedDirectExchange = new DirectExchange(VIDEO_ENCODER_UPLOADED_DIRECT_EXCHANGE_NAME);

        return BindingBuilder
                .bind(videoUploadedQueue)
                .to(videoUploadtedDirectExchange)
                .withQueueName();
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);

        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventApplicationListener(RabbitAdmin rabbitAdmin) {
        return event -> rabbitAdmin.initialize();
    }

}
