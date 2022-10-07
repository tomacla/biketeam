package info.tomacla.biketeam.domain.place;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.function.Function;

public class PlaceSorter {

    public static Comparator<PlaceAppearanceProjection> of(PlaceSorterOption sort) {
        if (PlaceSorterOption.RIDE_START.equals(sort)) {
            return new PlaceDateComparator(
                    PlaceAppearanceProjection::getRideStartPlaceAppearances,
                    PlaceAppearanceProjection::getLastRideStartPlaceAppearance
            );
        }
        if (PlaceSorterOption.RIDE_END.equals(sort)) {
            return new PlaceDateComparator(
                    PlaceAppearanceProjection::getRideEndPlaceAppearances,
                    PlaceAppearanceProjection::getLastRideEndPlaceAppearance
            );
        }
        if (PlaceSorterOption.TRIP_START.equals(sort)) {
            return new PlaceDateComparator(
                    PlaceAppearanceProjection::getTripStartPlaceAppearances,
                    PlaceAppearanceProjection::getLastTripStartPlaceAppearance
            );
        }
        if (PlaceSorterOption.TRIP_END.equals(sort)) {
            return new PlaceDateComparator(
                    PlaceAppearanceProjection::getTripEndPlaceAppearances,
                    PlaceAppearanceProjection::getLastTripEndPlaceAppearance
            );
        }
        return new PlaceNameComparator();
    }

    public static class PlaceNameComparator implements Comparator<PlaceAppearanceProjection> {

        @Override
        public int compare(PlaceAppearanceProjection p1, PlaceAppearanceProjection p2) {
            return p1.getName().compareToIgnoreCase(p2.getName());
        }

    }

    public static class PlaceDateComparator implements Comparator<PlaceAppearanceProjection> {

        Function<PlaceAppearanceProjection, LocalDate> dateSupplier;
        Function<PlaceAppearanceProjection, Integer> countSupplier;

        public PlaceDateComparator(Function<PlaceAppearanceProjection, Integer> countSupplier,
                                   Function<PlaceAppearanceProjection, LocalDate> dateSupplier) {
            this.countSupplier = countSupplier;
            this.dateSupplier = dateSupplier;
        }

        @Override
        public int compare(PlaceAppearanceProjection p1, PlaceAppearanceProjection p2) {

            Integer count1 = countSupplier.apply(p1);
            Integer count2 = countSupplier.apply(p2);

            if (count1 == 0 && count2 > 0) {
                return 1;
            }

            if (count1 > 0 && count2 == 0) {
                return -1;
            }

            LocalDate d1 = dateSupplier.apply(p1);
            LocalDate d2 = dateSupplier.apply(p2);

            if (d1 == null && d2 != null) {
                return 1;
            }

            if (d1 != null && d2 == null) {
                return -1;
            }

            if (d1 == null && d2 == null) {
                return 0;
            }

            return d1.compareTo(d2);

        }

    }

}