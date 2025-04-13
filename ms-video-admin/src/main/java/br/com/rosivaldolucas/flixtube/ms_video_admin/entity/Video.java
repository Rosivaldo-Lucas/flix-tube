package br.com.rosivaldolucas.flixtube.ms_video_admin.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "video", schema = "db_video_admin")
public class Video {

    @Id
    private String id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "duration", nullable = false)
    private Double duration;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "raw_location", nullable = false)
    private String rawLocation;

    @Column(name = "encoded_location")
    private String encodedLocation;

    @Column(name = "media_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private VideoStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant created_at;

    @Column(name = "updated_at", nullable = false)
    private Instant updated_at;

    protected Video() { }

    private Video(String title, String description, Double duration, String filename, String rawLocation) {
        Instant now = Instant.now();

        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.title = title;
        this.description = description;
        this.duration = duration;
        this.filename = filename;
        this.rawLocation = rawLocation;
        this.status = VideoStatus.PENDING;
        this.created_at = now;
        this.updated_at = now;
    }

    public static Video create(String title, String description, Double duration, String filename, String rawLocation) {
        return new Video(title, description, duration, filename, rawLocation);
    }

}
