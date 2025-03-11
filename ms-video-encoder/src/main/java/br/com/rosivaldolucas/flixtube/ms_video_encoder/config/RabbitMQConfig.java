package br.com.rosivaldolucas.flixtube.ms_video_encoder.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String FLIXTUBE_DIRECT_EXCHANGE_NAME = "flixtube.direct";
    public static final String FLIXTUBE_FANOUT_EXCHANGE_DLX_NAME = "fllixtube.fanout.dlx";
    public static final String FLIXTUBE_QUEUE_DLQ_NAME = "flixtube.queue.dlq";

    public static final String VIDEO_ENCODER_UPLOADED_QUEUE_NAME = "video-encoder.uploaded.queue";
    public static final String VIDEO_ENCODER_UPLOADED_ROUTING_KEY = "video-encoder.uploaded.rk";

    @Bean
    public FanoutExchange flixtubeFanoutExchange() {
        return new FanoutExchange(FLIXTUBE_FANOUT_EXCHANGE_DLX_NAME, true, false);
    }

    @Bean
    public Queue flixtubeQueueDlq() {
        return new Queue(FLIXTUBE_QUEUE_DLQ_NAME, true);
    }

    @Bean
    public Binding bindFlixtubeQueueDlqToFlixtubeFanoutExchange() {
        Queue flixtubeQueueDlq = this.flixtubeQueueDlq();
        FanoutExchange flixtubeFanoutExchange = this.flixtubeFanoutExchange();

        return BindingBuilder
                .bind(flixtubeQueueDlq)
                .to(flixtubeFanoutExchange);
    }

    @Bean
    public DirectExchange flixtubeDirectExchange() {
        return new DirectExchange(FLIXTUBE_DIRECT_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue videoEncoderUploadedQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", FLIXTUBE_FANOUT_EXCHANGE_DLX_NAME);

        return new Queue(VIDEO_ENCODER_UPLOADED_QUEUE_NAME, true, false, false, args);
    }

    @Bean
    public Binding bindVideoEncoderUploadedQueueToFlixtubeDirectExchange() {
        Queue videoUploadedQueue = this.videoEncoderUploadedQueue();
        DirectExchange videoUploadtedDirectExchange = new DirectExchange(FLIXTUBE_DIRECT_EXCHANGE_NAME);

        return BindingBuilder
                .bind(videoUploadedQueue)
                .to(videoUploadtedDirectExchange)
                .with(VIDEO_ENCODER_UPLOADED_ROUTING_KEY);
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
