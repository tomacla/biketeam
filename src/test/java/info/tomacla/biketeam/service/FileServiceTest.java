package info.tomacla.biketeam.service;

import info.tomacla.biketeam.service.file.FileService;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileServiceTest {

    @Test
    public void testStore() throws Exception {
        FileService fileService = create();
        Path source = Paths.get(fileService.getClass().getClassLoader().getResource("image-test.jpg").toURI());
        assertFalse(fileService.fileExists("test-store", "teamid", "new-file"));
        fileService.storeFile(source, "test-store", "teamid","new-file");
        assertTrue(fileService.fileExists("test-store", "teamid","new-file"));
        fileService.getFile("test-store", "teamid","new-file");
    }

    private FileService create() {
        return new FileService("/tmp/biketeam-test/" + UUID.randomUUID());
    }

}
