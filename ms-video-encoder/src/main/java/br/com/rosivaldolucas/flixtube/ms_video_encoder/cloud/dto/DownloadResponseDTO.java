package br.com.rosivaldolucas.flixtube.ms_video_encoder.cloud.dto;

import java.io.InputStream;

public record DownloadResponseDTO (
        String filename,
        String url,
        InputStream contentAsInputStream,
        byte[] contentAsByteArray,
        long contentLength,
        String contentType
) { }
