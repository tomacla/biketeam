package info.tomacla.biketeam.domain.ride;

import java.time.LocalDate;

public interface RideIdTitleDateProjection {

    String getId();

    String getTitle();

    LocalDate getDate();

}
