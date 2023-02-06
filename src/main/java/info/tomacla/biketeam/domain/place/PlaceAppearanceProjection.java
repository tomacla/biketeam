package info.tomacla.biketeam.domain.place;

import java.time.LocalDate;

public interface PlaceAppearanceProjection {

    String getTeamId();

    String getId();

    String getName();

    LocalDate getLastTripStartPlaceAppearance();

    LocalDate getLastTripEndPlaceAppearance();

    LocalDate getLastRideStartPlaceAppearance();

    LocalDate getLastRideEndPlaceAppearance();

    int getTripStartPlaceAppearances();

    int getTripEndPlaceAppearances();

    int getRideStartPlaceAppearances();

    int getRideEndPlaceAppearances();

    boolean isStartPlace();

    boolean isEndPlace();

}
