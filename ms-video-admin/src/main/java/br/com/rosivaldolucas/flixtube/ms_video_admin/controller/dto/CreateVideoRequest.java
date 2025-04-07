package br.com.rosivaldolucas.flixtube.ms_video_admin.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateVideoRequest {
    @NotBlank
    String title;

    @NotBlank
    String description;

    @NotNull
    Double duration;

    Resource resource;

    @Getter
    @Setter
    public static class Resource {
        String filename;
        byte[] content;
        String format;

        public Resource(String filename, byte[] content, String format) {
            this.filename = filename;
            this.content = content;
            this.format = format;
        }
    }

}
