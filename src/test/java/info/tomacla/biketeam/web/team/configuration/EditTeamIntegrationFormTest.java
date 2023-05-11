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
                .withHeatmapDisplay(true)
                .withHeatmapCenter(new Point(10.0, 5.0))
                .withMattermostChannelID("channelId")
                .withMattermostMessageChannelID("mchannelId")
                .withMattermostApiToken("apitoken")
                .withMattermostApiEndpoint("endpoint")
                .get();

        final EditTeamIntegrationForm.EditTeamIntegrationFormParser parser = form.parser();

        assertTrue(parser.isMattermostPublishPublications());
        assertTrue(parser.isMattermostPublishRides());
        assertTrue(parser.isMattermostPublishTrips());
        assertEquals("channelId", parser.getMattermostChannelID());
        assertEquals("mchannelId", parser.getMattermostMessageChannelID());
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
        assertNull(parser.getMattermostMessageChannelID());
        assertNull(parser.getMattermostApiEndpoint());
        assertNull(parser.getMattermostApiToken());
        assertNull(parser.getMattermostChannelID());
        assertFalse(parser.isHeatmapDisplay());
        assertNull(parser.getHeatmapCenter());

    }

}
