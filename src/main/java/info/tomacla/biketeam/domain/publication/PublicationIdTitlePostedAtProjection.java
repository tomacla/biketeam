package info.tomacla.biketeam.domain.publication;

import java.time.ZonedDateTime;

public interface PublicationIdTitlePostedAtProjection {

    String getId();

    String getTitle();

    ZonedDateTime getPostedAt();

}
