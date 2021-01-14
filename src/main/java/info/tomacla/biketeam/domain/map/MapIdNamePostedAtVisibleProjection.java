package info.tomacla.biketeam.domain.map;

import java.time.LocalDate;

public interface MapIdNamePostedAtVisibleProjection {

    String getId();

    String getName();

    LocalDate getPostedAt();

    boolean isVisible();

}
