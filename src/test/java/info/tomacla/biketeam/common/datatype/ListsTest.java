package info.tomacla.biketeam.common.datatype;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ListsTest {

    @Test
    public void testEmpty() {

        final IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class, () -> Lists.requireNonEmpty(new ArrayList<>(), "empty"));
        assertEquals(e1.getMessage(), "empty");

        final IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class, () -> Lists.requireNonEmpty(null, "empty"));
        assertEquals(e2.getMessage(), "empty");

        Lists.requireNonEmpty(List.of("foo"), "empty");


    }

    @Test
    public void testSize() {

        final IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class, () -> Lists.requireSizeOf(new ArrayList<>(), 3, "empty"));
        assertEquals(e1.getMessage(), "empty");

        final IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class, () -> Lists.requireSizeOf(null, 3, "empty"));
        assertEquals(e2.getMessage(), "empty");

        final IllegalArgumentException e3 = assertThrows(IllegalArgumentException.class, () -> Lists.requireSizeOf(List.of("foo", "bar"), 3, "empty"));
        assertEquals(e3.getMessage(), "empty");

        Lists.requireSizeOf(List.of("foo", "bar", "toto"), 3, "empty");
        Lists.requireSizeOf(List.of("foo", "bar", "toto", "tata"), 3, "empty");


    }

}
