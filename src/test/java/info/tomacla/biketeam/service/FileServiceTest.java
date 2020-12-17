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
        assertFalse(fileService.exists("test-store", "new-file"));
        fileService.store(source, "test-store", "new-file");
        assertTrue(fileService.exists("test-store", "new-file"));
        fileService.get("test-store", "new-file");
    }

    private FileService create() {
        return new FileService("/tmp/biketeam-test/" + UUID.randomUUID().toString());
    }

}
