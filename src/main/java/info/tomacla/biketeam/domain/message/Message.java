package info.tomacla.biketeam.domain.message;

import info.tomacla.biketeam.domain.user.User;

import java.time.ZonedDateTime;

public interface Message {

    String getId();

    String getContent();

    User getUser();

    ZonedDateTime getPublishedAt();

}
