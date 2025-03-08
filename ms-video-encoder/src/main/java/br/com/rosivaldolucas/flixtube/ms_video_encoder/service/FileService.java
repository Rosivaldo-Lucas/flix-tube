package br.com.rosivaldolucas.flixtube.ms_video_encoder.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class FileService {

    public void persistFile(String pathDir, String filename, InputStream content) {
        log.info("starting persisting file to path: {}/{}", pathDir, filename);
        Path path = Path.of(pathDir);

        try {
            Files.createDirectories(path);
            log.info("directory created: {}", path);
        } catch (IOException ex) {
            log.error("error creating directory: {}", path, ex);
            throw new RuntimeException("error creating directory: " + path, ex);
        }

        Path filePath = path.resolve(filename);

        try (OutputStream os = Files.newOutputStream(filePath)) {
            os.write(content.readAllBytes());
            log.info("file persisted successfully: {}", filePath);
        } catch (IOException ex) {
            log.error("error persisting file: {}", filePath, ex);
            throw new RuntimeException("error persisting file: " + filePath, ex);
        }

        log.info("end persisting");
    }

    public List<File> loadFiles(String pathDir) {
        log.info("starting loading files from path: {}", pathDir);
        List<File> paths = new ArrayList<>();

        Path path = Path.of(pathDir);
        File[] files = path.toFile().listFiles();

        if (files != null) {
            log.info("loaded {} files from directory: {}", files.length, pathDir);
            paths.addAll(Arrays.asList(files));
        } else {
            log.warn("no files found in directory: {}", pathDir);
        }

        log.info("end loading");
        return paths;
    }

    public void cleanDir(String pathDir) {
        log.info("starting cleaning directory: {}", pathDir);

        Path path = Path.of(pathDir);

        if (!Files.exists(path)) {
            log.warn("directory does not exist, skipping cleanup: {}", pathDir);
            return;
        }

        try (Stream<Path> files = Files.walk(path)) {
            files.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                            log.info("deleted file: {}", p);
                        } catch (IOException ex) {
                            log.error("error deleting file: {}", p, ex);
                            throw new RuntimeException("error deleting file: " + p, ex);
                        }
                    });
            log.info("directory cleaned successfully: {}", pathDir);
        } catch (IOException ex) {
            log.error("error cleaning directory: {}", pathDir, ex);
            throw new RuntimeException("error cleaning directory: " + pathDir, ex);
        }
        log.info("end cleaning");
    }

}
