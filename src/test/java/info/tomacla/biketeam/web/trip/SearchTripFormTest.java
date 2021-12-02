package info.tomacla.biketeam.web.trip;

import info.tomacla.biketeam.common.datatype.Dates;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SearchTripFormTest {

    @Test
    public void test() {

        final SearchTripForm form = SearchTripForm.builder()
                .withFrom(LocalDate.parse("2021-01-05"))
                .withTo(LocalDate.parse("2021-01-19"))
                .withPage(10)
                .withPageSize(100)
                .get();

        final SearchTripForm.SearchTripFormParser parser = form.parser();

        assertEquals(LocalDate.parse("2021-01-05").toString(), parser.getFrom().toString());
        assertEquals(LocalDate.parse("2021-01-19").toString(), parser.getTo().toString());
        assertEquals(10, parser.getPage());
        assertEquals(100, parser.getPageSize());

    }

    @Test
    public void testDefault() {

        final SearchTripForm form = SearchTripForm.builder().get();

        final SearchTripForm.SearchTripFormParser parser = form.parser();

        assertEquals(Dates.formatDate(LocalDate.now().minus(1, ChronoUnit.MONTHS)), parser.getFrom().toString());
        assertEquals(Dates.formatDate(LocalDate.now().plus(1, ChronoUnit.MONTHS)), parser.getTo().toString());
        assertEquals(0, parser.getPage());
        assertEquals(10, parser.getPageSize());

    }

}
