package info.tomacla.biketeam.domain.message;

public interface MessageHolder {

    String getId();

    String getTeamId();

    MessageTargetType getMessageType();

}
