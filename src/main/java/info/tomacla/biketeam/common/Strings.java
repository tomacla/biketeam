package info.tomacla.biketeam.common;

import java.text.Normalizer;
import java.util.Objects;

public class Strings {

    public static String requireNonBlank(String s, String message) {
        Objects.requireNonNull(s);
        if (s.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return s;
    }

    public static String requireNonBlankOrNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s;
    }

    public static String requireNonBlankOrDefault(String s, String defaultValue) {
        if (s == null || s.isBlank()) {
            return defaultValue;
        }
        return s;
    }

    public static String permatitleFromString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replaceAll("\\?", "");
        normalized = normalized.replaceAll("\\#", "");
        normalized = normalized.replaceAll("\\&", "");
        normalized = normalized.replaceAll("\\s+", "_");
        return normalized;
    }

}
