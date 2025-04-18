package br.com.rosivaldolucas.flixtube.ms_video_encoder.messaging.dto;

public record VideoUploadedEventDTO (
        String transactionId,
        String inputPath,
        String filename
) { }
