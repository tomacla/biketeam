package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.domain.team.Visibility;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EditTeamGeneralFormTest {

    @Test
    public void test() {

        final EditTeamGeneralForm form = EditTeamGeneralForm.builder()
                .withVisibility(Visibility.PRIVATE)
                .withName("name")
                .withDescription("desc super")
                .get();

        final EditTeamGeneralForm.EditTeamGeneralFormParser parser = form.parser();

        assertEquals("name", parser.getName());
        assertEquals("desc super", parser.getDescription());
        assertEquals(Visibility.PRIVATE, parser.getVisibility());

    }

    @Test
    public void testDefault() {

        final EditTeamGeneralForm form = EditTeamGeneralForm.builder().get();

        final EditTeamGeneralForm.EditTeamGeneralFormParser parser = form.parser();

        assertNull(parser.getName());
        assertNull(parser.getDescription());
        assertEquals(Visibility.PUBLIC, parser.getVisibility());

    }

}
