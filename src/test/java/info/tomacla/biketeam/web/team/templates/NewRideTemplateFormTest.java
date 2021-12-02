package info.tomacla.biketeam.web.team.templates;

import info.tomacla.biketeam.domain.ride.RideType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NewRideTemplateFormTest {

    @Test
    public void test() {

        final NewRideTemplateForm form = NewRideTemplateForm.builder(1)
                .withId("id")
                .withName("name")
                .withIncrement(200)
                .withType(RideType.RACE)
                .withDescription("desc ride")
                .get();

        final NewRideTemplateForm.NewRideTemplateFormParser parser = form.parser();

        assertEquals("id", parser.getId());
        assertEquals("name", parser.getName());
        assertEquals(RideType.RACE, parser.getType());
        assertEquals("desc ride", parser.getDescription());
        assertEquals(200, parser.getIncrement());
        assertEquals(1, parser.getGroups().size());

    }

    @Test
    public void testDefault() {

        final NewRideTemplateForm form = NewRideTemplateForm.builder(1).get();

        final NewRideTemplateForm.NewRideTemplateFormParser parser = form.parser();

        assertEquals("new", parser.getId());
        assertNull(parser.getName());
        assertEquals(RideType.REGULAR, parser.getType());
        assertNull(parser.getDescription());
        assertNull(parser.getIncrement());
        assertEquals(1, parser.getGroups().size());

    }

}
