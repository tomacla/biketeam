package info.tomacla.biketeam.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FileExtensionTest {

    @Test
    public void test() {
        assertTrue(FileExtension.findByFileName("foobar.png").isPresent());
        assertEquals(FileExtension.PNG, FileExtension.findByFileName("foobar.png").get());
        assertFalse(FileExtension.findByFileName("foobar.java").isPresent());
    }

}
