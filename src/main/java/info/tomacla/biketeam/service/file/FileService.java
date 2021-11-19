package info.tomacla.biketeam.service.file;

import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.FileRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.TimeUnit;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final String fileRepository;

    @Autowired
    public FileService(@Value("${file.repository}") String fileRepository) {
        this.fileRepository = fileRepository;
    }

    public Optional<FileExtension> fileExists(String directory, String teamId, String fileNameWithoutExtension, List<FileExtension> extensions) {
        for (FileExtension extension : extensions) {
            if (fileExists(directory, teamId, fileNameWithoutExtension + extension.getExtension())) {
                return Optional.of(extension);
            }
        }
        return Optional.empty();
    }

    public boolean fileExists(String directory, String teamId, String fileName) {
        return Files.exists(Path.of(fileRepository, directory, teamId, fileName));
    }

    public Path getDirectory(String directory, String teamId) {
        try {
            Files.createDirectories(Path.of(fileRepository, directory, teamId));
            return Path.of(fileRepository, directory, teamId);
        } catch (IOException e) {
            log.error("Unable to get directory : " + teamId, e);
            throw new RuntimeException("Unable to get directory : " + teamId, e);
        }
    }

    public Path getFile(String directory, String teamId, String fileName) {
        try {
            Files.createDirectories(Path.of(fileRepository, directory, teamId));
            return Path.of(fileRepository, directory, teamId, fileName);
        } catch (IOException e) {
            log.error("Unable to get file : " + fileName, e);
            throw new RuntimeException("Unable to get file : " + fileName, e);
        }
    }

    public void deleteFile(String directory, String teamId, String fileName) {
        try {
            Files.deleteIfExists(Path.of(fileRepository, directory, teamId, fileName));
        } catch (IOException e) {
            log.error("Unable to delete file : " + fileName, e);
            throw new RuntimeException("Unable to delete file : " + fileName, e);
        }
    }

    public void moveFile(String directory, String teamId, String oldFileName, String newFileName) {
        try {
            Files.move(Path.of(fileRepository, directory, teamId, oldFileName), Path.of(fileRepository, directory, teamId, newFileName));
        } catch (IOException e) {
            log.error("Unable to move file : " + oldFileName, e);
            throw new RuntimeException("Unable to move file : " + oldFileName, e);
        }
    }

    public void storeFile(Path file, String directory, String teamId, String fileName) {
        try {
            Files.createDirectories(Path.of(fileRepository, directory, teamId));
            Files.copy(file, Path.of(fileRepository, directory, teamId, fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Unable to store file : " + fileName, e);
            throw new RuntimeException("Unable to store file : " + file.toString(), e);
        }
    }

    public void storeFile(InputStream file, String directory, String teamId, String fileName) {
        try {
            Files.createDirectories(Path.of(fileRepository, directory, teamId));
            Files.copy(file, Path.of(fileRepository, directory, teamId, fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Unable to store file : " + fileName, e);
            throw new RuntimeException("Unable to store file : " + file.toString(), e);
        }
    }

    public Path getTempFile(String prefix, String suffix) {
        try {
            return Files.createTempFile(getTmpDirectory(), prefix, suffix);
        } catch (IOException e) {
            log.error("Unable to get temp file", e);
            throw new RuntimeException(e);
        }
    }

    public Path getTempFileFromInputStream(InputStream is, String prefix, String suffix) {
        try {
            Path tmp = this.getTempFile(prefix, suffix);
            Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        } catch (IOException e) {
            log.error("Unable to get temp file from input stream", e);
            throw new RuntimeException(e);
        }
    }

    public Path getTempFileFromInputStream(InputStream is) {
        return this.getTempFileFromInputStream(is, UUID.randomUUID().toString(), ".tmp");
    }

    private Path getTmpDirectory() {
        return Path.of(System.getProperty("java.io.tmpdir"), "biketeam");
    }

    public void cleanTmpDirectory() {
        try {
            long cutOff = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000);
            Files.list(getTmpDirectory())
                    .filter(path -> {
                        try {
                            return Files.isRegularFile(path) && Files.getLastModifiedTime(path).to(TimeUnit.MILLISECONDS) < cutOff;
                        } catch (IOException ex) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.error("Unable to delete file " + path, e);
                        }
                    });
        } catch (IOException e) {
            log.error("Unable to clean tmp directory", e);
        }
    }

    @PostConstruct
    public void init() throws Exception {

        log.info("Initializing appliation directories");

        Files.createDirectories(Path.of(fileRepository));
        for (String subDirectory : FileRepositories.list()) {
            Files.createDirectories(Path.of(fileRepository, subDirectory));
        }
        Files.createDirectories(getTmpDirectory());

    }

}
