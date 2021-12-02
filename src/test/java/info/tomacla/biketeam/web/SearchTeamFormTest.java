package info.tomacla.biketeam.web;

import info.tomacla.biketeam.common.data.Country;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SearchTeamFormTest {

    @Test
    public void test() {

        final SearchTeamForm form = SearchTeamForm.builder()
                .withCity("city")
                .withCountry(Country.ES)
                .withPageSize(10)
                .withPage(0)
                .withName("foo")
                .get();

        final SearchTeamForm.SearchTeamFormParser parser = form.parser();

        assertEquals("city", parser.getCity());
        assertEquals("foo", parser.getName());
        assertEquals(Country.ES, parser.getCountry());
        assertEquals(0, parser.getPage());
        assertEquals(10, parser.getPageSize());

    }

    @Test
    public void testDefault() {

        final SearchTeamForm form = SearchTeamForm.builder().get();
        final SearchTeamForm.SearchTeamFormParser parser = form.parser();

        assertNull(parser.getCity());
        assertNull(parser.getName());
        assertNull(parser.getCountry());
        assertEquals(0, parser.getPage());
        assertEquals(9, parser.getPageSize());

    }

}
