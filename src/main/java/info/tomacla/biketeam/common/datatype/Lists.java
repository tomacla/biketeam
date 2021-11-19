package info.tomacla.biketeam.common.datatype;

import java.util.Collection;
import java.util.Objects;

public class Lists {

    public static void requireNonEmpty(Collection<?> list, String message) {
        Objects.requireNonNull(list, message);
        if (list.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requireSizeOf(Collection<?> list, int minimumSize, String message) {
        Objects.requireNonNull(list, message);
        if (list.isEmpty() || list.size() < minimumSize) {
            throw new IllegalArgumentException(message);
        }
    }

}
