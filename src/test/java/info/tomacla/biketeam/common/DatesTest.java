package info.tomacla.biketeam.common;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.*;

public class DatesTest {

    @Test
    public void test() {
        assertEquals("12:32", Dates.formatTime(LocalTime.of(12,32)));
        assertEquals("12:32", Dates.formatTime(LocalTime.of(12,32, 28)));
        assertEquals("2021-05-12", Dates.formatDate(LocalDate.of(2021,05,12)));
        assertEquals("2021-05-12", Dates.formatZonedDate(ZonedDateTime.of(LocalDate.of(2021,05,12), LocalTime.of(12,32), ZoneId.of("Europe/Paris"))));
        assertEquals("12:32", Dates.formatZonedTime(ZonedDateTime.of(LocalDate.of(2021,05,12), LocalTime.of(12,32), ZoneId.of("Europe/Paris"))));
        assertEquals("12 mai 2021", Dates.frenchDateFormat(LocalDate.of(2021,05,12)));
    }

}
