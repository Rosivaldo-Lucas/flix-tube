package br.com.rosivaldolucas.flixtube.ms_video_admin.cloud;

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

    public void uploadFile(String bucket, String key, InputStream content) {
        try {
            this.s3Template.upload(bucket, key, content);
        } catch (Exception ex) {
            log.error("error uploading file to S3 - bucket={}, key={}", bucket, key, ex);
            throw new RuntimeException("error uploading file to S3", ex);
        }
    }

}
