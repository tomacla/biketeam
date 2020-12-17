package info.tomacla.biketeam.common;

import java.util.List;
import java.util.Objects;

public class Lists {

    public static void requireNonEmpty(List<?> list, String message) {
        Objects.requireNonNull(list, message);
        if (list.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

}
