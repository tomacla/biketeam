package info.tomacla.biketeam.common.datatype;

import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatesTest {

    @Test
    public void test() {
        assertEquals("12:32", Dates.formatTime(LocalTime.of(12, 32)));
        assertEquals("12:32", Dates.formatTime(LocalTime.of(12, 32, 28)));
        assertEquals("2021-05-12", Dates.formatDate(LocalDate.of(2021, 5, 12)));
        assertEquals("2021-05-12", Dates.formatZonedDate(ZonedDateTime.of(LocalDate.of(2021, 5, 12), LocalTime.of(12, 32), ZoneId.of("Europe/Paris"))));
        assertEquals("12:32", Dates.formatZonedTime(ZonedDateTime.of(LocalDate.of(2021, 5, 12), LocalTime.of(12, 32), ZoneId.of("Europe/Paris"))));
        assertEquals("12 mai 2021", Dates.frenchDateFormat(LocalDate.of(2021, 5, 12)));
        assertEquals("2021-05-01", Dates.formatZonedDateInTimezone(ZonedDateTime.parse("2021-05-01T12:00:00Z"), ZoneId.of("Europe/Paris")));
        assertEquals("12:00", Dates.formatZonedTimeInTimezone(ZonedDateTime.of(LocalDateTime.parse("2021-05-01T12:00:00"), ZoneId.of("Europe/Paris")), ZoneId.of("Europe/Paris")));
        assertEquals(ZonedDateTime.parse("2021-05-01T12:00:00Z"), Dates.parseZonedDateTimeInUTC("2021-05-01", "12:00", ZoneOffset.UTC));
    }

}
