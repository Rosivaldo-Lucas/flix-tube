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
        log.info("Start fragment processing...");
        log.info("Input file to fragmentation: {}. Output file result the fragmentation: {}", inputFilePath, outputFilePath);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("mp4fragment", inputFilePath, outputFilePath);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Fragmentation process failed. Process exited with code {}", exitCode);
                throw new RuntimeException("Fragmentation process failed");
            }

            log.info("Fragmentation process finished");
        } catch (IOException ex) {
            log.error("Error during fragmentation", ex);
            throw new RuntimeException("Error during fragmentation", ex);
        } catch (InterruptedException ex) {
            log.error("Fragmentation interrupted", ex);
            throw new RuntimeException("Fragmentation interrupted", ex);
        }
    }

    public void encode(String inputFilePath, String outputDir) {
        log.info("Start encode processing...");
        log.info("Input file to encode: {}. Output dir result the encoding: {}", inputFilePath, outputDir);

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
                log.error("Encoding process failed. Process exited with code {}", exitCode);
                throw new RuntimeException("Encoding process failed");
            }

            log.info("Encoding process finished");
        } catch (IOException ex) {
            log.error("Error during encoding", ex);
            throw new RuntimeException("Error during encoding", ex);
        } catch (InterruptedException ex) {
            log.error("Encoding interrupted", ex);
            throw new RuntimeException("Encoding interrupted", ex);
        }
    }

}
