package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.common.data.Timezone;
import info.tomacla.biketeam.domain.team.WebPage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EditTeamConfigurationFormTest {

    @Test
    public void test() {

        final EditTeamConfigurationForm form = EditTeamConfigurationForm.builder()
                .withTripsVisible(true)
                .withFeedVisible(true)
                .withRidesVisible(true)
                .withDefaultPage(WebPage.MAPS)
                .withDefaultSearchTags(List.of("toot", "taat"))
                .withTimezone("Ukraine")
                .get();

        final EditTeamConfigurationForm.EditTeamConfigurationFormParser parser = form.parser();

        assertEquals("Ukraine", parser.getTimezone());
        assertEquals(List.of("toot", "taat"), parser.getDefaultSearchTags());
        assertEquals(WebPage.MAPS, parser.getDefaultPage());
        assertTrue(parser.isRidesVisible());
        assertTrue(parser.isFeedVisible());
        assertTrue(parser.isTripsVisible());

    }

    @Test
    public void testDefault() {

        final EditTeamConfigurationForm form = EditTeamConfigurationForm.builder().get();

        final EditTeamConfigurationForm.EditTeamConfigurationFormParser parser = form.parser();

        assertEquals(Timezone.DEFAULT_TIMEZONE, parser.getTimezone());
        assertEquals(0, parser.getDefaultSearchTags().size());
        assertEquals(WebPage.FEED, parser.getDefaultPage());
        assertFalse(parser.isRidesVisible());
        assertFalse(parser.isFeedVisible());
        assertFalse(parser.isTripsVisible());

    }

}
