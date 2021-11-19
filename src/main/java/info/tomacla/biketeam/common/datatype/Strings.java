package info.tomacla.biketeam.common.datatype;

import org.springframework.util.ObjectUtils;

import java.text.Normalizer;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {

    public static final Pattern EMAIL = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE);

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

    public static String normalizePermalink(String permalink) {
        if (ObjectUtils.isEmpty(permalink)) {
            return null;
        }
        String normalized = Normalizer.normalize(permalink, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replaceAll("[\\\\!?#&$%'()*+,/:;<=>@\\[\\]\"^`{|}~]+", "");
        normalized = normalized.replaceAll("\\s+", "_");
        return normalized;
    }

    public static boolean isEmail(String s) {
        if (s == null || s.isBlank()) {
            return false;
        }

        Matcher matcher = EMAIL.matcher(s.trim());
        return matcher.find();
    }

    public static String requireEmail(String s) {
        if (!Strings.isEmail(s)) {
            throw new IllegalArgumentException("Invalid email");
        }
        return s.trim();
    }

    public static String requireEmailOrNull(String s) {
        if (!Strings.isEmail(s)) {
            return null;
        }
        return s.trim();
    }

}
