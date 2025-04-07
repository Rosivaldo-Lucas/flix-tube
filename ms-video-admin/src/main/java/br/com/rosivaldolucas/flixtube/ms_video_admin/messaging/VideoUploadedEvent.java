package br.com.rosivaldolucas.flixtube.ms_video_admin.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class VideoUploadedEvent {

    private String resourceId;
    private String inputFilename;

}
