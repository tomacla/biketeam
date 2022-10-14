package info.tomacla.biketeam.common.file;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FileExtensionTest {

    @Test
    public void test() {
        assertTrue(FileExtension.findByFileName(null).isEmpty());
        assertTrue(FileExtension.findByFileName(" ").isEmpty());
        assertTrue(FileExtension.findByFileName("foobar.png").isPresent());
        assertEquals(FileExtension.PNG, FileExtension.findByFileName("foobar.png").get());
        assertEquals(FileExtension.JPEG, FileExtension.findByFileName("foobar.JPEG").get());
        assertEquals(FileExtension.JPEG, FileExtension.findByFileName("foobar.png.JpeG").get());
        assertFalse(FileExtension.findByFileName("foobar.java").isPresent());
        assertEquals(FileExtension.PNG, FileExtension.findByMimeType("image/png").get());
    }

}
