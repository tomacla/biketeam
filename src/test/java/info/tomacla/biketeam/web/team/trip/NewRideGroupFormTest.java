package info.tomacla.biketeam.web.team.trip;

import info.tomacla.biketeam.common.geo.Point;
import info.tomacla.biketeam.web.team.ride.NewRideGroupForm;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NewRideGroupFormTest {

    @Test
    public void test() {

        final NewRideGroupForm form = NewRideGroupForm.builder()
                .withId("id")
                .withName("group")
                .withLowerSpeed(10)
                .withUpperSpeed(50)
                .withMeetingLocation("nantes")
                .withMeetingPoint(new Point(5.0, 10.0))
                .withMeetingTime(LocalTime.parse("16:30"))
                .withMapId("mapid")
                .withMapName("mapname")
                .get();

        final NewRideGroupForm.NewRideGroupFormParser parser = form.parser();

        assertEquals("id", parser.getId());
        assertEquals("group", parser.getName());
        assertEquals(10, parser.getLowerSpeed());
        assertEquals(50, parser.getUpperSpeed());
        assertEquals("nantes", parser.getMeetingLocation());
        assertEquals(new Point(5.0, 10.0), parser.getMeetingPoint());
        assertEquals(LocalTime.parse("16:30"), parser.getMeetingTime());
        assertEquals("mapid", parser.getMapId());

    }

    @Test
    public void testDefault() {

        final NewRideGroupForm form = NewRideGroupForm.builder().get();

        final NewRideGroupForm.NewRideGroupFormParser parser = form.parser();

        assertNull(parser.getId());
        assertNull(parser.getName());
        assertEquals(28, parser.getLowerSpeed());
        assertEquals(30, parser.getUpperSpeed());
        assertNull(parser.getMeetingLocation());
        assertNull(parser.getMeetingPoint());
        assertEquals(LocalTime.parse("12:00"), parser.getMeetingTime());
        assertNull(parser.getMapId());

    }

}