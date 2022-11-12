package info.tomacla.biketeam.domain.feed;

import info.tomacla.biketeam.common.data.PublishedStatus;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public interface FeedEntity {

    PublishedStatus getPublishedStatus();

    ZonedDateTime getPublishedAt();

    LocalDate getDate();

    FeedType getFeedType();

}
