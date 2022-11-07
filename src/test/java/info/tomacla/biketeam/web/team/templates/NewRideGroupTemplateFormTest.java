package info.tomacla.biketeam.web.team.templates;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NewRideGroupTemplateFormTest {

    @Test
    public void test() {

        final NewRideGroupTemplateForm form = NewRideGroupTemplateForm.builder()
                .withId("id")
                .withName("group")
                .withLowerSpeed(10)
                .withUpperSpeed(50)
                .withMeetingTime(LocalTime.parse("16:30"))
                .get();

        final NewRideGroupTemplateForm.NewRideGroupTemplateFormParser parser = form.parser();

        assertEquals("id", parser.getId());
        assertEquals("group", parser.getName());
        assertEquals(10, parser.getLowerSpeed());
        assertEquals(50, parser.getUpperSpeed());
        assertEquals(LocalTime.parse("16:30"), parser.getMeetingTime());

    }

    @Test
    public void testDefault() {

        final NewRideGroupTemplateForm form = NewRideGroupTemplateForm.builder().get();

        final NewRideGroupTemplateForm.NewRideGroupTemplateFormParser parser = form.parser();

        assertNull(parser.getId());
        assertNull(parser.getName());
        assertEquals(28, parser.getLowerSpeed());
        assertEquals(30, parser.getUpperSpeed());
        assertEquals(LocalTime.parse("12:00"), parser.getMeetingTime());

    }

}
