package br.com.rosivaldolucas.flixtube.ms_video_admin.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VideoUploadedEvent {

    private String transactionId;
    private String inputPath;
    private String filename;

}
