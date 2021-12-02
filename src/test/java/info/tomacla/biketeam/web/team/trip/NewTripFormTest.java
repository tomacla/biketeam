package info.tomacla.biketeam.web.team.trip;

import info.tomacla.biketeam.common.geo.Point;
import info.tomacla.biketeam.domain.map.MapType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NewTripFormTest {

    @Test
    public void test() {

        final NewTripForm form = NewTripForm.builder(ZonedDateTime.parse("2021-01-05T12:10:05Z"), ZoneOffset.UTC)
                .withPermalink("perm")
                .withStartDate(LocalDate.parse("2021-01-01"))
                .withEndDate(LocalDate.parse("2021-01-05"))
                .withId("id")
                .withTitle("title")
                .withType(MapType.ROAD)
                .withDescription("desc ride")
                .withLowerSpeed(10)
                .withUpperSpeed(50)
                .withMeetingLocation("nantes")
                .withMeetingPoint(new Point(5.0, 10.0))
                .withMeetingTime(LocalTime.parse("16:30"))
                .get();

        final NewTripForm.NewTripFormParser parser = form.parser();

        assertEquals("perm", parser.getPermalink());
        assertEquals("id", parser.getId());
        assertEquals("title", parser.getTitle());
        assertEquals(MapType.ROAD, parser.getType());
        assertEquals("desc ride", parser.getDescription());
        assertEquals(10, parser.getLowerSpeed());
        assertEquals(50, parser.getUpperSpeed());
        assertEquals("nantes", parser.getMeetingLocation());
        assertEquals(new Point(5.0, 10.0), parser.getMeetingPoint());
        assertEquals(LocalTime.parse("16:30"), parser.getMeetingTime());
        assertEquals(LocalDate.parse("2021-01-01"), parser.getStartDate());
        assertEquals(LocalDate.parse("2021-01-05"), parser.getEndDate());
        assertEquals(2, parser.getStages("t1", null).size());
        assertEquals("2021-01-05T12:10Z", parser.getPublishedAt(ZoneOffset.UTC).toString());

    }

    @Test
    public void testDefault() {

        final NewTripForm form = NewTripForm.builder(ZonedDateTime.parse("2021-01-05T12:10:05Z"), ZoneOffset.UTC).get();

        final NewTripForm.NewTripFormParser parser = form.parser();

        assertNull(parser.getPermalink());
        assertEquals("new", parser.getId());
        assertNull(parser.getTitle());
        assertEquals(MapType.ROAD, parser.getType());
        assertNull(parser.getDescription());
        assertEquals(28, parser.getLowerSpeed());
        assertEquals(30, parser.getUpperSpeed());
        assertNull(parser.getMeetingLocation());
        assertNull(parser.getMeetingPoint());
        assertEquals(LocalTime.parse("08:00"), parser.getMeetingTime());
        assertEquals(LocalDate.parse("2021-01-05"), parser.getStartDate());
        assertEquals(LocalDate.parse("2021-01-06"), parser.getEndDate());
        assertEquals(2, parser.getStages("t1", null).size());
        assertEquals("2021-01-05T12:10Z", parser.getPublishedAt(ZoneOffset.UTC).toString());

    }

}
