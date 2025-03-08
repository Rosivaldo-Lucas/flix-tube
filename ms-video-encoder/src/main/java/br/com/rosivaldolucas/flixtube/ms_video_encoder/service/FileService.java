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
        Path path = Path.of(pathDir);

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException();
        }

        Path filePath = path.resolve(filename);

        try (OutputStream os = Files.newOutputStream(filePath)) {
            os.write(content.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<File> loadFiles(String pathDir) {
        List<File> paths = new ArrayList<>();

        Path path = Path.of(pathDir);
        File[] files = path.toFile().listFiles();

        if (files != null) {
            paths.addAll(Arrays.asList(files));
        }

        return paths;
    }

    public void cleanDir(String pathDir) {
        Path path = Path.of(pathDir);

        if (!Files.exists(path)) {
            return;
        }

        try (Stream<Path> files = Files.walk(path)) {
            files.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException();
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

}
