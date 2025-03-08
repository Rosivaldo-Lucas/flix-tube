package br.com.rosivaldolucas.flixtube.ms_video_encoder.cloud;

import br.com.rosivaldolucas.flixtube.ms_video_encoder.cloud.dto.DownloadResponseDTO;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Integration {

    private final S3Template s3Template;

    public DownloadResponseDTO downloadFile(String bucket, String key) {
        log.info("starting downloading file from S3: bucket={}, key={}", bucket, key);
        try {
            S3Resource s3Resource = this.s3Template.download(bucket, key);
            log.info("file downloaded successfully - filename: {}, URL: {}", s3Resource.getFilename(), s3Resource.getURL());
            log.info("end downloading file from S3");

            return new DownloadResponseDTO(
                    s3Resource.getFilename(),
                    s3Resource.getURL().toString(),
                    s3Resource.getInputStream(),
                    s3Resource.getContentAsByteArray(),
                    s3Resource.contentLength(),
                    s3Resource.contentType()
            );
        } catch (Exception ex) {
            log.error("error downloading file from S3 - bucket={}, key={}", bucket, key, ex);
            throw new RuntimeException("error downloading file from S3", ex);
        }
    }

    public void uploadFile(String bucket, String key, InputStream content) {
        try {
            this.s3Template.upload(bucket, key, content);
        } catch (Exception ex) {
            log.error("error uploading file to S3 - bucket={}, key={}", bucket, key, ex);
            throw new RuntimeException("error uploading file to S3", ex);
        }
    }

}
