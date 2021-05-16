package info.tomacla.biketeam.domain.ride;

import info.tomacla.biketeam.common.PublishedStatus;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public interface RideIdTitleDateProjection {

    String getId();

    String getTitle();

    LocalDate getDate();

    ZonedDateTime getPublishedAt();

    PublishedStatus getPublishedStatus();

}
