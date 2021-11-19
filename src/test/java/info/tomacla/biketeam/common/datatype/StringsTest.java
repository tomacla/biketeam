package info.tomacla.biketeam.common.datatype;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringsTest {

    @Test
    public void test() {
        assertEquals("a_full_title", Strings.normalizePermalink("a full title"));
        assertEquals("a_full_title", Strings.normalizePermalink("a  full title"));
        assertEquals("un_enumere_en_francais", Strings.normalizePermalink("un énuméré en français"));
        assertEquals("With_Char_not_wanted", Strings.normalizePermalink("With ? Char # not & wanted"));
        assertEquals("N-Peloton", Strings.normalizePermalink("N-Peloton"));
        assertEquals(".-_.-_", Strings.normalizePermalink("!\"#$%&'.()*+-,/:;<=>?@[\\]_^`{|}~!\"#$%&'.()*+-,/:;<=>?@[\\]_^`{|}~"));
    }

}
