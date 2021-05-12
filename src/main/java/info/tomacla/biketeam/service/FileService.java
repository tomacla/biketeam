package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.FileExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    private final String fileRepository;

    @Autowired
    public FileService(@Value("${file.repository}") String fileRepository) {
        this.fileRepository = fileRepository;
    }

    public Optional<FileExtension> exists(String fileName, List<FileExtension> extensions) {
        for (FileExtension extension : extensions) {
            if (exists(fileName + extension.getExtension())) {
                return Optional.of(extension);
            }
        }
        return Optional.empty();
    }

    public Optional<FileExtension> exists(String directory, String fileName, List<FileExtension> extensions) {
        for (FileExtension extension : extensions) {
            if (exists(directory, fileName + extension.getExtension())) {
                return Optional.of(extension);
            }
        }
        return Optional.empty();
    }

    public boolean exists(String fileName) {
        return Files.exists(Path.of(fileRepository, fileName));
    }

    public boolean exists(String directory, String fileName) {
        return Files.exists(Path.of(fileRepository, directory, fileName));
    }

    public Path get(String fileName) {
        return Path.of(fileRepository, fileName);
    }

    public Path get(String directory, String fileName) {
        try {
            Files.createDirectories(Path.of(fileRepository, directory));
            return Path.of(fileRepository, directory, fileName);
        } catch (IOException e) {
            throw new RuntimeException("Unable to get file : " + fileName, e);
        }
    }

    public void store(Path file, String fileName) {
        try {
            Files.copy(file, Path.of(fileRepository, fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Unable to store file : " + file, e);
        }
    }

    public void store(Path file, String directory, String fileName) {
        try {
            Files.createDirectories(Path.of(fileRepository, directory));
            Files.copy(file, Path.of(fileRepository, directory, fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Unable to store file : " + file.toString(), e);
        }
    }

    public Path getTempFile() {
        try {
            return Files.createTempFile(getBiketeamTempPath(), UUID.randomUUID().toString(), ".tmp");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getTempFileFromInputStream(InputStream is) {
        try {
            Path tmp = this.getTempFile();
            Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void initTmpDir() throws Exception {

        Files.createDirectories(Path.of(fileRepository));
        Files.createDirectories(getBiketeamTempPath());

        // copy default logo
        if (!Files.exists(Path.of(fileRepository, "logo.jpg"))) {
            Files.copy(getClass().getResourceAsStream("/default-logo.jpg"), Path.of(fileRepository, "logo.jpg"), StandardCopyOption.REPLACE_EXISTING);
        }

    }

    private Path getBiketeamTempPath() {
        return Path.of(System.getProperty("java.io.tmpdir"), "biketeam");
    }

}
