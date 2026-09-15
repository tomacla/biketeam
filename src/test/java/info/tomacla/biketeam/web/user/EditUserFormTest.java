package info.tomacla.biketeam.web.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EditUserFormTest {

    @Test
    public void test() {

        final EditUserForm editUserForm = EditUserForm.builder()
                .withEmailPublishTrips(true)
                .withEmailPublishRides(true)
                .withEmailPublishPublications(true)
                .get();

        final EditUserForm.EditUserFormParser parser = editUserForm.parser();

        assertTrue(parser.isEmailPublishPublications());
        assertTrue(parser.isEmailPublishRides());
        assertTrue(parser.isEmailPublishTrips());

    }

    @Test
    public void testDefault() {

        final EditUserForm editUserForm = EditUserForm.builder().get();

        final EditUserForm.EditUserFormParser parser = editUserForm.parser();

        assertFalse(parser.isEmailPublishPublications());
        assertFalse(parser.isEmailPublishRides());
        assertFalse(parser.isEmailPublishTrips());

    }

}
