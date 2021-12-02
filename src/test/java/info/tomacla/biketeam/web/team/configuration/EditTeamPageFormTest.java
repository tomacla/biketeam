package info.tomacla.biketeam.web.team.configuration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EditTeamPageFormTest {

    @Test
    public void test() {

        final EditTeamPageForm form = EditTeamPageForm.builder().withMarkdownPage("toto").get();

        final EditTeamPageForm.EditTeamPageFormParser parser = form.parser();

        assertEquals("toto", parser.getMarkdownPage());

    }

    @Test
    public void testDefault() {

        final EditTeamPageForm form = EditTeamPageForm.builder().get();

        final EditTeamPageForm.EditTeamPageFormParser parser = form.parser();

        assertNull(parser.getMarkdownPage());

    }

}
