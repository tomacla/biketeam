package info.tomacla.biketeam.domain.reaction;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ReactionContentTest {

    @Test
    public void testsurrogates() {
        assertEquals(ReactionContent.HAPPY_1.surrogates()[0], 0xD83D);
        assertEquals(ReactionContent.HAPPY_1.surrogates()[1], 0xDE03);
    }

    @Test
    public void testvalueofsurrogates() {
        assertEquals(ReactionContent.HAPPY_1, ReactionContent.valueOfSurrogates(new int[]{0xD83D, 0xDE03}));
        assertNull(ReactionContent.valueOfSurrogates(new int[]{0xD83D, 0xDE08}));
    }

    @Test
    public void testvalueofunicode() {
        assertEquals(ReactionContent.HAPPY_1, ReactionContent.valueOfUnicode("1F603"));
        assertEquals(ReactionContent.HAPPY_1, ReactionContent.valueOfUnicode("U+1f603"));
        assertEquals(ReactionContent.HAPPY_1, ReactionContent.valueOfUnicode("u+1F603"));
        assertNull(ReactionContent.valueOfUnicode("1F608"));
    }


}
