package br.com.rosivaldolucas.flixtube.ms_video_encoder.cloud;

import br.com.rosivaldolucas.flixtube.ms_video_encoder.cloud.dto.DownloadResponseDTO;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Integration {

    private final S3Template s3Template;

    public DownloadResponseDTO downloadFile(String bucket, String key) {
        try {
            S3Resource s3Resource = this.s3Template.download(bucket, key);

            return new DownloadResponseDTO(
                    s3Resource.getFilename(),
                    s3Resource.getURL().toString(),
                    s3Resource.getInputStream(),
                    s3Resource.getContentAsByteArray(),
                    s3Resource.contentLength(),
                    s3Resource.contentType()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
