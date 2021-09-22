package info.tomacla.biketeam.common;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class Dates {

    public static final DateTimeFormatter frenchFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.FRANCE);
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public static String formatTime(LocalTime temporal) {
        return temporal.truncatedTo(ChronoUnit.SECONDS).format(timeFormatter);
    }

    public static String formatDate(LocalDate temporal) {
        return temporal.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String formatZonedDateInTimezone(ZonedDateTime temporal, ZoneId timezone) {
        return formatDate(temporal.withZoneSameInstant(timezone).toLocalDate());
    }

    public static String formatZonedTimeInTimezone(ZonedDateTime temporal, ZoneId timezone) {
        return formatTime(temporal.withZoneSameInstant(timezone).toLocalTime());
    }

    public static String formatZonedDate(ZonedDateTime temporal) {
        return formatDate(temporal.toLocalDate());
    }

    public static String formatZonedTime(ZonedDateTime temporal) {
        return formatTime(temporal.toLocalTime());
    }

    public static String frenchDateFormat(LocalDate temporal) {
        return temporal.format(frenchFormatter);
    }

    public static ZonedDateTime parseZonedDateTimeInUTC(String date, String time, ZoneId timezone) {
        return ZonedDateTime.of(LocalDateTime.parse(date + "T" + time), timezone).withZoneSameInstant(ZoneOffset.UTC);
    }

}
