package info.tomacla.biketeam.domain.trip;

import info.tomacla.biketeam.common.PublishedStatus;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public interface TripIdTitleDateProjection {

    String getId();

    String getTitle();

    LocalDate getStartDate();

    ZonedDateTime getPublishedAt();

    PublishedStatus getPublishedStatus();

}
