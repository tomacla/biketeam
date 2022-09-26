package info.tomacla.biketeam.common.amqp;

public interface RoutingKeys {

    String PUBLICATION_PUBLISHED = "publication_published";
    String RIDE_MESSAGE_PUBLISHED = "ride_message_published";
    String TRIP_MESSAGE_PUBLISHED = "trip_message_published";
    String RIDE_PUBLISHED = "ride_published";
    String TRIP_PUBLISHED = "trip_published";
    String TASK_PUBLISH_RIDES = "task_publish_rides";
    String TASK_PUBLISH_TRIPS = "task_publish_trips";
    String TASK_PUBLISH_PUBLICATIONS = "task_publish_publications";
    String TASK_CLEAN_TMP_FILES = "task_clean_tmp_files";
    String TASK_CLEAN_TEAM_FILES = "task_clean_team_files";
    String TASK_GENERATE_HEATMAPS = "taks_generate_heatmaps";


}
