package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.common.geo.Point;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EditTeamIntegrationFormTest {

    @Test
    public void test() {

        final EditTeamIntegrationForm form = EditTeamIntegrationForm.builder()
                .withMattermostPublishTrips(true)
                .withMattermostPublishRides(true)
                .withMattermostPublishPublications(true)
                .withFacebookPublishTrips(true)
                .withFacebookPublishRides(true)
                .withFacebookPublishPublications(true)
                .withHeatmapDisplay(true)
                .withHeatmapCenter(new Point(10.0, 5.0))
                .withMattermostChannelID("channelId")
                .withMattermostApiToken("apitoken")
                .withMattermostApiEndpoint("endpoint")
                .withFacebookGroupDetails(false)
                .get();

        final EditTeamIntegrationForm.EditTeamIntegrationFormParser parser = form.parser();

        assertTrue(parser.isMattermostPublishPublications());
        assertTrue(parser.isMattermostPublishRides());
        assertTrue(parser.isMattermostPublishTrips());
        assertTrue(parser.isFacebookPublishPublications());
        assertTrue(parser.isFacebookPublishRides());
        assertTrue(parser.isFacebookPublishTrips());
        assertFalse(parser.isFacebookGroupDetails());
        assertEquals("channelId", parser.getMattermostChannelID());
        assertEquals("apitoken", parser.getMattermostApiToken());
        assertEquals("endpoint", parser.getMattermostApiEndpoint());
        assertTrue(parser.isHeatmapDisplay());
        assertEquals(new Point(10.0, 5.0), parser.getHeatmapCenter());


    }

    @Test
    public void testDefault() {

        final EditTeamIntegrationForm form = EditTeamIntegrationForm.builder().get();

        final EditTeamIntegrationForm.EditTeamIntegrationFormParser parser = form.parser();

        assertFalse(parser.isMattermostPublishPublications());
        assertFalse(parser.isMattermostPublishRides());
        assertFalse(parser.isMattermostPublishTrips());
        assertFalse(parser.isFacebookPublishPublications());
        assertFalse(parser.isFacebookPublishRides());
        assertFalse(parser.isFacebookPublishTrips());
        assertFalse(parser.isFacebookGroupDetails());
        assertNull(parser.getMattermostChannelID());
        assertNull(parser.getMattermostChannelID());
        assertNull(parser.getMattermostChannelID());
        assertFalse(parser.isHeatmapDisplay());
        assertNull(parser.getHeatmapCenter());

    }

}
