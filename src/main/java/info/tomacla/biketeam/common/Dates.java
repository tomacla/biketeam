package info.tomacla.biketeam.common;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class Dates {

    public static final DateTimeFormatter frenchFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.FRANCE);
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public static String formatTime() {
        return formatTime(null);
    }

    public static String formatTime(LocalTime temporal) {
        var target = temporal == null ? LocalTime.now() : temporal;
        return target.truncatedTo(ChronoUnit.SECONDS).format(timeFormatter);
    }

    public static String formatDate() {
        return formatDate(null);
    }

    public static String formatDate(LocalDate temporal) {
        var target = temporal == null ? LocalDate.now() : temporal;
        return target.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String formatZonedDate(ZonedDateTime temporal) {
        var target = temporal == null ? ZonedDateTime.now() : temporal;
        return formatDate(target.toLocalDate());
    }

    public static String formatZonedTime(ZonedDateTime temporal) {
        var target = temporal == null ? ZonedDateTime.now() : temporal;
        return formatTime(target.toLocalTime());
    }

    public static String frenchDateFormat(LocalDate temporal) {
        var target = temporal == null ? LocalDate.now() : temporal;
        return target.format(frenchFormatter);
    }

}
