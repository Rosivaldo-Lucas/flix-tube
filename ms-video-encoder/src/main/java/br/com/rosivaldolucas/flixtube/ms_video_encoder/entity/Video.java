package br.com.rosivaldolucas.flixtube.ms_video_encoder.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "video", schema = "db_video_encoder")
public class Video {

    @Id
    private String id;

    @Column(name = "resource_id")
    private String resourceId;

    @Column(name = "bucket")
    private String bucket;

    @Column(name = "input_file_path")
    private String inputFilePath;

    @Column(name = "input_filename")
    private String inputFilename;

    @Column(name = "output_file_path")
    private String outputFilePath;

    @Column(name = "status")
    private String status;

    @Column(name = "error")
    private String error;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Video() { }

    public Video(String resourceId, String bucket, String inputFilePath, String inputFilename, String outputFilePath) {
        LocalDateTime now = LocalDateTime.now();

        this.id = UUID.randomUUID().toString().replaceAll("-", "");
        this.resourceId = resourceId;
        this.bucket = bucket;
        this.inputFilePath = inputFilePath;
        this.inputFilename = inputFilename;
        this.outputFilePath = String.format("%s/%s", outputFilePath, this.id);
        this.status = "PENDING";
        this.error = "";
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void updateStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void addError(String error) {
        this.error = error;
        this.updatedAt = LocalDateTime.now();
    }

}
