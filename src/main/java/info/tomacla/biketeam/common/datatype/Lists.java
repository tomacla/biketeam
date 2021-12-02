package info.tomacla.biketeam.common.datatype;

import java.util.Collection;

public class Lists {

    public static void requireNonEmpty(Collection<?> list, String message) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requireSizeOf(Collection<?> list, int minimumSize, String message) {
        if (list == null || list.isEmpty() || list.size() < minimumSize) {
            throw new IllegalArgumentException(message);
        }
    }

}
