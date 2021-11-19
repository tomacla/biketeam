package info.tomacla.biketeam.domain.map;

import java.time.LocalDate;

public interface MapProjection {

    String getTeamId();

    String getId();

    String getPermalink();

    String getName();

    LocalDate getPostedAt();

}
