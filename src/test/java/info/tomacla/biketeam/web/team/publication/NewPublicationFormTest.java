package info.tomacla.biketeam.web.team.publication;

import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NewPublicationFormTest {

    @Test
    public void test() {

        final NewPublicationForm form = NewPublicationForm.builder(ZonedDateTime.parse("2021-01-05T12:10:05Z"), ZoneOffset.UTC)
                .withPublishedAt(ZonedDateTime.parse("2021-01-05T12:10:05Z"), ZoneOffset.UTC)
                .withContent("content")
                .withId("id")
                .withTitle("title")
                .get();

        final NewPublicationForm.NewPublicationFormParser parser = form.parser();

        assertEquals("title", parser.getTitle());
        assertEquals("id", parser.getId());
        assertEquals("content", parser.getContent());
        assertEquals("2021-01-05T12:10Z", parser.getPublishedAt(ZoneOffset.UTC).toString());

    }

    @Test
    public void testDefault() {

        final NewPublicationForm form = NewPublicationForm.builder(ZonedDateTime.parse("2021-01-05T12:10:05Z"), ZoneOffset.UTC).get();

        final NewPublicationForm.NewPublicationFormParser parser = form.parser();

        assertNull(parser.getTitle());
        assertEquals("new", parser.getId());
        assertNull(parser.getContent());
        assertEquals("2021-01-05T12:10Z", parser.getPublishedAt(ZoneOffset.UTC).toString());

    }

}
