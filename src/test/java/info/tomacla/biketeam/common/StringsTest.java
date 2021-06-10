package info.tomacla.biketeam.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringsTest {

    @Test
    public void test() {
        assertEquals("a_full_title", Strings.permatitleFromString("a full title"));
        assertEquals("a_full_title", Strings.permatitleFromString("a  full title"));
        assertEquals("un_enumere_en_francais", Strings.permatitleFromString("un énuméré en français"));
        assertEquals("With_Char_not_wanted", Strings.permatitleFromString("With ? Char # not & wanted"));
    }

}
