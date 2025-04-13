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
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("mp4fragment", inputFilePath, outputFilePath);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("fragmentation process failed");
            }

        } catch (IOException ex) {
            throw new RuntimeException("error during fragmentation", ex);
        } catch (InterruptedException ex) {
            throw new RuntimeException("fragmentation interrupted", ex);
        }
        log.info("end fragmentation");
    }

    public void encode(String inputFilePath, String outputDir) {
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
                throw new RuntimeException("encoding process failed");
            }
        } catch (IOException ex) {
            throw new RuntimeException("error during encoding");
        } catch (InterruptedException ex) {
            throw new RuntimeException("encoding interrupted", ex);
        }
    }

}
