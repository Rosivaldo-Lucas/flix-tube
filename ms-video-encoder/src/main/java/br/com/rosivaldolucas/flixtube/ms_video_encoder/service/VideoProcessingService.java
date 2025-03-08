package br.com.rosivaldolucas.flixtube.ms_video_encoder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoProcessingService {

    public void fragment(String inputFilePath, String outputFilePath) {
        log.info("starting fragmentation: input={}, output={}", inputFilePath, outputFilePath);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("mp4fragment", inputFilePath, outputFilePath);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("fragmentation failed with exit code: {}", exitCode);
                throw new RuntimeException("fragmentation process failed");
            }

            log.info("fragmentation completed successfully");
        } catch (IOException ex) {
            log.error("error during fragmentation", ex);
            throw new RuntimeException("error during fragmentation", ex);
        } catch (InterruptedException ex) {
            log.error("fragmentation interrupted", ex);
            throw new RuntimeException("fragmentation interrupted", ex);
        }
        log.info("end fragmentation");
    }

    public void encode(String inputFilePath, String outputDir) {
        log.info("starting encoding: input={}, outputDir={}", inputFilePath, outputDir);

        String[] args = {
                inputFilePath,
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
                log.error("encoding failed with exit code: {}", exitCode);
                throw new RuntimeException("encoding process failed");
            }

            log.info("encoding completed successfully");
        } catch (IOException ex) {
            log.error("error during encoding", ex);
            throw new RuntimeException("error during encoding");
        } catch (InterruptedException ex) {
            log.error("encoding interrupted", ex);
            throw new RuntimeException("encoding interrupted", ex);
        }
        log.info("end encoding");
    }

}
