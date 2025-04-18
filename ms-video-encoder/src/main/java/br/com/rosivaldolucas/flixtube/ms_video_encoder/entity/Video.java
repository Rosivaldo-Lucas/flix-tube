package br.com.rosivaldolucas.flixtube.ms_video_encoder.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "video", schema = "db_video_encoder")
public class Video {

    @Id
    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "bucket", nullable = false)
    private String bucket;

    @Column(name = "input_path", nullable = false)
    private String inputPath;

    @Column(name = "output_path", nullable = false)
    private String outputPath;

    @Column(name = "input_filename", nullable = false)
    private String inputFilename;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "error")
    private String error;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Video() { }

    public Video(String transactionId, String bucket, String inputPath, String outputPath, String inputFilename) {
        LocalDateTime now = LocalDateTime.now();

        this.transactionId = transactionId;
        this.bucket = bucket;
        this.inputPath = inputPath;
        this.outputPath = outputPath;
        this.inputFilename = inputFilename;
        this.status = "PENDING";
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
