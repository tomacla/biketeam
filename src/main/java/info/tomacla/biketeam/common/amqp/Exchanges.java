package info.tomacla.biketeam.common.amqp;

public interface Exchanges {

    String TASK = "biketeam.task";
    String PUBLISH_RIDE = "biketeam.publish_ride";
    String PUBLISH_TRIP = "biketeam.publish_trip";
    String PUBLISH_PUBLICATION = "biketeam.publish_publication";
    String PUBLISH_RIDE_MESSAGE = "biketeam.ride_message";
    String PUBLISH_TRIP_MESSAGE = "biketeam.trip_message";

}
