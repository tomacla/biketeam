package info.tomacla.biketeam.domain.publication;

import info.tomacla.biketeam.common.PublishedStatus;

import java.time.ZonedDateTime;

public interface PublicationIdTitlePostedAtProjection {

    String getId();

    String getTitle();

    ZonedDateTime getPublishedAt();

    PublishedStatus getPublishedStatus();

}
