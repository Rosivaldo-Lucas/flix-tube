package br.com.rosivaldolucas.flixtube.ms_video_admin.messaging.dto;

import br.com.rosivaldolucas.flixtube.ms_video_admin.entity.VideoStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class VideoProcessedEvent {

    private String transactionId;
    private String inputPath;
    private String outputPath;
    private String filename;
    private VideoStatus status;
    private String error;
    private LocalDateTime createdAt;
    private LocalDateTime processingStartedAt;
    private LocalDateTime processingEndedAt;

}
