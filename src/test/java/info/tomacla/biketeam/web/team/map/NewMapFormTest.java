package info.tomacla.biketeam.web.team.map;

import info.tomacla.biketeam.domain.map.MapType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NewMapFormTest {

    @Test
    public void test() {

        final NewMapForm form = NewMapForm.builder()
                .withPermalink("permallink")
                .withId("id")
                .withName("name")
                .withTags(List.of("toot", "taat"))
                .withType(MapType.GRAVEL)
                .withVisible(false)
                .get();

        final NewMapForm.NewMapFormParser parser = form.parser();

        assertEquals("permallink", parser.getPermalink());
        assertEquals("id", parser.getId());
        assertEquals("name", parser.getName());
        assertEquals(List.of("toot", "taat"), parser.getTags());
        assertEquals(MapType.GRAVEL, parser.getType());
        assertFalse(parser.isVisible());

    }

    @Test
    public void testDefault() {

        final NewMapForm form = NewMapForm.builder().get();

        final NewMapForm.NewMapFormParser parser = form.parser();

        assertNull(parser.getPermalink());
        assertEquals("new", parser.getId());
        assertNull(parser.getName());
        assertEquals(0, parser.getTags().size());
        assertEquals(MapType.ROAD, parser.getType());
        assertTrue(parser.isVisible());

    }

}
