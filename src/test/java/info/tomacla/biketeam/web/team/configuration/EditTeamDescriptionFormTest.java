package info.tomacla.biketeam.web.team.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EditTeamDescriptionFormTest {

    @Test
    public void test() {

        final EditTeamDescriptionForm form = EditTeamDescriptionForm.builder()
                .withAddressCity("address")
                .withAddressPostalCode("19120")
                .withAddressStreetLine("street")
                .withEmail("foo@bar.com")
                .withFacebook("facebook")
                .withTwitter("twitter")
                .withOther("hello world")
                .withPhoneNumber("0123456789")
                .get();

        final EditTeamDescriptionForm.EditTeamDescriptionFormParser parser = form.parser();

        assertEquals("address", parser.getAddressCity());
        assertEquals("19120", parser.getAddressPostalCode());
        assertEquals("street", parser.getAddressStreetLine());
        assertEquals("foo@bar.com", parser.getEmail());
        assertEquals("facebook", parser.getFacebook());
        assertEquals("twitter", parser.getTwitter());
        assertEquals("hello world", parser.getOther());
        assertEquals("0123456789", parser.getPhoneNumber());

    }

    @Test
    public void testDefault() {

        final EditTeamDescriptionForm form = EditTeamDescriptionForm.builder().get();

        final EditTeamDescriptionForm.EditTeamDescriptionFormParser parser = form.parser();

        assertNull(parser.getAddressCity());
        assertNull(parser.getAddressPostalCode());
        assertNull(parser.getAddressStreetLine());
        assertNull(parser.getEmail());
        assertNull(parser.getFacebook());
        assertNull(parser.getTwitter());
        assertNull(parser.getOther());
        assertNull(parser.getPhoneNumber());

    }

}
