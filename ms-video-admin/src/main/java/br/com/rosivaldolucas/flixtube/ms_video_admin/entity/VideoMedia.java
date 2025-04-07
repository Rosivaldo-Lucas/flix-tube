package br.com.rosivaldolucas.flixtube.ms_video_admin.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Getter
@Embeddable
public class VideoMedia {

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "raw_location", nullable = false)
    private String rawLocation;

    @Column(name = "encoded_location", nullable = false)
    private String encodedLocation;

    @Column(name = "media_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaStatus status;

    protected VideoMedia() { }

    private VideoMedia(String filename, String rawLocation, MediaStatus status) {
        this.filename = filename;
        this.rawLocation = rawLocation;
        this.encodedLocation = "test";
        this.status = status;
    }

    public static VideoMedia create(String filename, String rawLocation, MediaStatus status) {
        return new VideoMedia(filename, rawLocation, status);
    }

}
