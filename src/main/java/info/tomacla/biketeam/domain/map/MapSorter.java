package info.tomacla.biketeam.domain.map;

import java.util.Comparator;

public class MapSorter {

    public static Comparator<Map> of(MapSorterOption sort) {
        if (MapSorterOption.SHORT.equals(sort)) {
            return new MapDistanceComparator();
        }
        if (MapSorterOption.LONG.equals(sort)) {
            return new MapDistanceComparator().reversed();
        }
        if (MapSorterOption.FLAT.equals(sort)) {
            return new MapElevationComparator();
        }
        if (MapSorterOption.HILLY.equals(sort)) {
            return new MapElevationComparator().reversed();
        }
        if (MapSorterOption.BEST_RATED.equals(sort)) {
            return new MapRatingComparator().reversed();
        }
        if (MapSorterOption.WORST_RATED.equals(sort)) {
            return new MapRatingComparator();
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

    public static class MapRatingComparator implements Comparator<Map> {

        @Override
        public int compare(Map m1, Map m2) {
            // First compare by average rating
            int ratingComparison = Double.compare(
                    m1.getAverageRating() != null ? m1.getAverageRating() : 0.0,
                    m2.getAverageRating() != null ? m2.getAverageRating() : 0.0
            );
            
            // If ratings are equal, compare by rating count
            if (ratingComparison == 0) {
                return Integer.compare(
                        m1.getRatingCount() != null ? m1.getRatingCount() : 0,
                        m2.getRatingCount() != null ? m2.getRatingCount() : 0
                );
            }
            
            return ratingComparison;
        }
    }

}
