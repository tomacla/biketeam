package info.tomacla.biketeam.domain.ride;

import info.tomacla.biketeam.common.data.PublishedStatus;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public interface RideProjection {

    String getId();

    String getTitle();

    LocalDate getDate();

    ZonedDateTime getPublishedAt();

    PublishedStatus getPublishedStatus();

}
