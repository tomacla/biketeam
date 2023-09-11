package info.tomacla.biketeam.domain.publication;

import info.tomacla.biketeam.common.data.PublishedStatus;

import java.time.ZonedDateTime;

public interface PublicationProjection {

    String getId();

    String getTitle();

    ZonedDateTime getPublishedAt();

    PublishedStatus getPublishedStatus();

    boolean isAllowRegistration();

}
