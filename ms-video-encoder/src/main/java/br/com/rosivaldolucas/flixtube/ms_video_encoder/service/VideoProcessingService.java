package br.com.rosivaldolucas.flixtube.ms_video_encoder.service;

import br.com.rosivaldolucas.flixtube.ms_video_encoder.cloud.S3Integration;
import br.com.rosivaldolucas.flixtube.ms_video_encoder.cloud.dto.DownloadResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoProcessingService {

    private final S3Integration s3Integration;

    public DownloadResponseDTO download(String bucket, String key) {
        return this.s3Integration.downloadFile(bucket, key);
    }

    public void fragment(String inputFilePath, String outputFilePath) {
        String inputPathWithExtension = String.format("%s.mp4", inputFilePath);
        String outputPathWithExtension = String.format("%s.frag", outputFilePath);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("mp4fragment", inputPathWithExtension, outputPathWithExtension);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException();
            }
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException();
        }
    }

    public void encode(String inputFilePath, String outputDir) {
        String inputPathWithExtension = String.format("%s.frag", inputFilePath);

        String[] args = {
                inputPathWithExtension,
                "--use-segment-timeline",
                "-o", outputDir,
                "-f",
                "--exec-dir", "/opt/bento4/bin/"
        };

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("mp4dash", args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException();
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException();
        }
    }

    public void upload(String inputFilePath, String outputFilePath) {
        String inputPathWithExtension = String.format("%s.mpd", inputFilePath);
    }

}
