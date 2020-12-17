package info.tomacla.biketeam.domain.navigationmap;

import java.util.Comparator;

public class MapSorter {

    public static Comparator<Map> of(String sort) {
        if ("short".equals(sort)) {
            return new MapDistanceComparator();
        }
        if ("long".equals(sort)) {
            return new MapDistanceComparator().reversed();
        }
        if ("flat".equals(sort)) {
            return new MapElevationComparator();
        }
        if ("hilly".equals(sort)) {
            return new MapElevationComparator().reversed();
        }
        return new MapPostedAtComparator().reversed();
    }

    public static class MapDistanceComparator implements Comparator<Map> {

        @Override
        public int compare(Map m1, Map m2) {
            return Double.compare(m1.getLength(), m2.getLength());
        }
    }

    public static class MapElevationComparator implements Comparator<Map> {

        @Override
        public int compare(Map m1, Map m2) {
            return Double.compare(m1.getPositiveElevation(), m2.getPositiveElevation());
        }
    }

    public static class MapPostedAtComparator implements Comparator<Map> {

        @Override
        public int compare(Map m1, Map m2) {
            return m1.getPostedAt().compareTo(m2.getPostedAt());
        }
    }

}
