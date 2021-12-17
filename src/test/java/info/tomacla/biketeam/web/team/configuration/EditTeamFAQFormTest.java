package info.tomacla.biketeam.web.team.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EditTeamFAQFormTest {

    @Test
    public void test() {

        final EditTeamFAQForm form = EditTeamFAQForm.builder().withMarkdownPage("toto").get();

        final EditTeamFAQForm.EditTeamPageFormParser parser = form.parser();

        assertEquals("toto", parser.getMarkdownPage());

    }

    @Test
    public void testDefault() {

        final EditTeamFAQForm form = EditTeamFAQForm.builder().get();

        final EditTeamFAQForm.EditTeamPageFormParser parser = form.parser();

        assertNull(parser.getMarkdownPage());

    }

}
