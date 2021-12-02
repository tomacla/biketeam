package info.tomacla.biketeam.common.datatype;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringsTest {

    @Test
    public void blankValidationTest() {
        Exception e1 = assertThrows(IllegalArgumentException.class, () -> Strings.requireNonBlank("", "blank"));
        assertEquals(e1.getMessage(), "blank");

        Exception e2 = assertThrows(IllegalArgumentException.class, () -> Strings.requireNonBlank(" ", "blank"));
        assertEquals(e2.getMessage(), "blank");

        Exception e3 = assertThrows(IllegalArgumentException.class, () -> Strings.requireNonBlank(null, "blank"));
        assertEquals(e3.getMessage(), "blank");

        assertEquals("non blank", Strings.requireNonBlank(" non blank ", "blank"));

        assertNull(Strings.requireNonBlankOrNull(""));
        assertEquals("non blank", Strings.requireNonBlankOrNull(" non blank "));

        assertEquals("non blank", Strings.requireNonBlankOrDefault(" non blank ", "blank"));
        assertEquals("blank", Strings.requireNonBlankOrDefault("  ", "blank"));

        assertTrue(Strings.isBlank("toto", "  ", "tata"));
        assertFalse(Strings.isBlank("toto", " tutu ", "tata"));

    }

    @Test
    public void emailValidationTest() {
        assertThrows(IllegalArgumentException.class, () -> Strings.requireEmail("foo"));
        assertThrows(IllegalArgumentException.class, () -> Strings.requireEmail("foo@bar"));
        assertThrows(IllegalArgumentException.class, () -> Strings.requireEmail(null));
        assertEquals("foo@bar.com", Strings.requireEmail(" foo@bar.com"));
        assertEquals("foo@bar.com", Strings.requireEmailOrNull(" foo@bar.com"));
        assertNull(Strings.requireEmailOrNull("foo@bar"));
    }

    @Test
    public void testNormalize() {
        assertNull(Strings.normalizePermalink(" "));
        assertNull(Strings.normalizePermalink(null));
        assertEquals("a_full_title", Strings.normalizePermalink("a full title"));
        assertEquals("a_full_title", Strings.normalizePermalink("a  full title"));
        assertEquals("un_enumere_en_francais", Strings.normalizePermalink("un énuméré en français"));
        assertEquals("With_Char_not_wanted", Strings.normalizePermalink("With ? Char # not & wanted"));
        assertEquals("N-Peloton", Strings.normalizePermalink("N-Peloton"));
        assertEquals(".-_.-_", Strings.normalizePermalink("!\"#$%&'.()*+-,/:;<=>?@[\\]_^`{|}~!\"#$%&'.()*+-,/:;<=>?@[\\]_^`{|}~"));
    }

}
