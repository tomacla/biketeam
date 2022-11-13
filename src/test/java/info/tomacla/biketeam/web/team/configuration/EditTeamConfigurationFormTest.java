package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.common.data.Timezone;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EditTeamConfigurationFormTest {

    @Test
    public void test() {

        final EditTeamConfigurationForm form = EditTeamConfigurationForm.builder()
                .withFeedVisible(true)
                .withDefaultSearchTags(List.of("toot", "taat"))
                .withTimezone("Ukraine")
                .get();

        final EditTeamConfigurationForm.EditTeamConfigurationFormParser parser = form.parser();

        assertEquals("Ukraine", parser.getTimezone());
        assertEquals(List.of("toot", "taat"), parser.getDefaultSearchTags());
        assertTrue(parser.isFeedVisible());

    }

    @Test
    public void testDefault() {

        final EditTeamConfigurationForm form = EditTeamConfigurationForm.builder().get();

        final EditTeamConfigurationForm.EditTeamConfigurationFormParser parser = form.parser();

        assertEquals(Timezone.DEFAULT_TIMEZONE, parser.getTimezone());
        assertEquals(0, parser.getDefaultSearchTags().size());
        assertFalse(parser.isFeedVisible());

    }

}
