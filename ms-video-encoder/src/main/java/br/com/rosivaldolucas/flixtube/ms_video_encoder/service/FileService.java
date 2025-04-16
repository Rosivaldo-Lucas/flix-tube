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
        log.info("Start persisting file: {} on directory: {}", filename, pathDir);
        Path path = Path.of(pathDir);

        try {
            Files.createDirectories(path);
        } catch (IOException ex) {
            log.error("Error creating directory: {}", path, ex);
            throw new RuntimeException("error creating directory: " + path, ex);
        }

        Path filePath = path.resolve(filename);

        try (OutputStream os = Files.newOutputStream(filePath)) {
            os.write(content.readAllBytes());
            log.info("Successfully persisted file: {} on directory: {}", filename, pathDir);
        } catch (IOException ex) {
            log.error("Error persisting file: {} on directory: {}", filename, pathDir, ex);
            throw new RuntimeException("error persisting file: " + filePath, ex);
        }
    }

    public List<File> loadFiles(String pathDir) {
        log.info("Start loading files from path: {}", pathDir);
        List<File> paths = new ArrayList<>();

        Path path = Path.of(pathDir);
        File[] files = path.toFile().listFiles();

        if (files != null) {
            paths.addAll(Arrays.asList(files));
            log.info("{} files loaded from {}", files.length, path.toAbsolutePath());
        } else {
            log.info("0 files loaded from {}", path.toAbsolutePath());
        }

        return paths;
    }

    public void cleanDir(String pathDir) {
        log.info("Start cleaning up directory: {}", pathDir);
        Path path = Path.of(pathDir);

        if (!Files.exists(path)) {
            log.info("{} directory does not exist", path.toAbsolutePath());
            return;
        }

        try (Stream<Path> files = Files.walk(path)) {
            files.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ex) {
                            log.error("Error deleting file: {}", p, ex);
                            throw new RuntimeException("Error deleting file: " + p, ex);
                        }
                    });
            log.info("Successfully cleaned up directory: {}", path.toAbsolutePath());
        } catch (IOException ex) {
            log.error("Error cleaning up directory: {}", pathDir, ex);
            throw new RuntimeException("Error cleaning directory: " + pathDir, ex);
        }
    }

}
