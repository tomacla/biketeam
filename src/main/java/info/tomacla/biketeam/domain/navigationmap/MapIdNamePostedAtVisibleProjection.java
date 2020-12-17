package info.tomacla.biketeam.domain.navigationmap;

import java.time.LocalDate;

public interface MapIdNamePostedAtVisibleProjection {

    String getId();

    String getName();

    LocalDate getPostedAt();

    boolean isVisible();

}
