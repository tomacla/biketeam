package info.tomacla.biketeam.web.team.ride;

import info.tomacla.biketeam.domain.ride.RideType;
import info.tomacla.biketeam.domain.template.RideGroupTemplate;
import info.tomacla.biketeam.domain.template.RideTemplate;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NewRideFormTest {

    @Test
    public void test() {

        final NewRideForm form = NewRideForm.builder(1, ZonedDateTime.parse("2021-01-05T12:10:05Z"), ZoneOffset.UTC)
                .withPermalink("perm")
                .withDate(LocalDate.parse("2021-01-01"))
                .withId("id")
                .withTitle("title")
                .withType(RideType.RACE)
                .withDescription("desc ride")
                .get();

        final NewRideForm.NewRideFormParser parser = form.parser();

        assertEquals("perm", parser.getPermalink());
        assertEquals("id", parser.getId());
        assertEquals("title", parser.getTitle());
        assertEquals(RideType.RACE, parser.getType());
        assertEquals("desc ride", parser.getDescription());
        assertEquals(LocalDate.parse("2021-01-01"), parser.getDate());
        assertEquals(1, parser.getGroups("t1", null).size());
        assertEquals("2021-01-05T12:10Z", parser.getPublishedAt(ZoneOffset.UTC).toString());

    }

    @Test
    public void testFromTemplate() {

        RideGroupTemplate g1 = new RideGroupTemplate();
        g1.setName("toot");
        RideGroupTemplate g2 = new RideGroupTemplate();
        g2.setName("taat");

        RideTemplate template = new RideTemplate();
        template.setName("template");
        template.setDescription("desc");
        template.setType(RideType.RACE);
        template.setIncrement(100);
        template.setGroups(Set.of(g1, g2));

        g1.setRideTemplate(template);
        g2.setRideTemplate(template);

        final NewRideForm form = NewRideForm.builder(template, ZonedDateTime.parse("2021-01-05T12:10:05Z"), ZoneOffset.UTC)
                .withPermalink("perm")
                .withDate(LocalDate.parse("2021-01-01"))
                .withId("id")
                .get();

        final NewRideForm.NewRideFormParser parser = form.parser();

        assertEquals("perm", parser.getPermalink());
        assertEquals("id", parser.getId());
        assertEquals("template #100", parser.getTitle());
        assertEquals(RideType.RACE, parser.getType());
        assertEquals("desc", parser.getDescription());
        assertEquals(LocalDate.parse("2021-01-01"), parser.getDate());
        assertEquals(2, parser.getGroups("t1", null).size());
        assertEquals("2021-01-05T12:10Z", parser.getPublishedAt(ZoneOffset.UTC).toString());

    }

    @Test
    public void testDefault() {

        final NewRideForm form = NewRideForm.builder(1, ZonedDateTime.parse("2021-01-05T12:10:05Z"), ZoneOffset.UTC).get();

        final NewRideForm.NewRideFormParser parser = form.parser();

        assertNull(parser.getPermalink());
        assertEquals("new", parser.getId());
        assertNull(parser.getTitle());
        assertEquals(RideType.REGULAR, parser.getType());
        assertNull(parser.getDescription());
        assertEquals(LocalDate.parse("2021-01-05"), parser.getDate());
        assertEquals(1, parser.getGroups("t1", null).size());
        assertEquals("2021-01-05T12:10Z", parser.getPublishedAt(ZoneOffset.UTC).toString());

    }

}
