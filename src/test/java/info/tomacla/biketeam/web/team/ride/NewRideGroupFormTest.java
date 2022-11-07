package info.tomacla.biketeam.web.team.ride;

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
                .withMeetingTime(LocalTime.parse("16:30"))
                .withMapId("mapid")
                .withMapName("mapname")
                .get();

        final NewRideGroupForm.NewRideGroupFormParser parser = form.parser();

        assertEquals("id", parser.getId());
        assertEquals("group", parser.getName());
        assertEquals(10, parser.getLowerSpeed());
        assertEquals(50, parser.getUpperSpeed());
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
        assertEquals(LocalTime.parse("12:00"), parser.getMeetingTime());
        assertNull(parser.getMapId());

    }

}
