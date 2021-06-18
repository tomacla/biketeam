package info.tomacla.biketeam.service;

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
        assertFalse(fileService.exists("test-store", "teamid", "new-file"));
        fileService.store(source, "test-store", "teamid","new-file");
        assertTrue(fileService.exists("test-store", "teamid","new-file"));
        fileService.get("test-store", "teamid","new-file");
    }

    private FileService create() {
        return new FileService("/tmp/biketeam-test/" + UUID.randomUUID().toString());
    }

}
