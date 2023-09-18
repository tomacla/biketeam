package info.tomacla.biketeam.common.amqp;

public interface Queues {

    String QUEUE_PREFIX = "biketeam.";
    String PUBLICATION_PUBLISHED_MAIL = QUEUE_PREFIX + "publication_published_mail";
    String PUBLICATION_PUBLISHED_MATTERMOST = QUEUE_PREFIX + "publication_published_mattermost";
    String RIDE_MESSAGE_PUBLISHED_NOTIFICATION = QUEUE_PREFIX + "ride_message_published_notification";
    String RIDE_MESSAGE_PUBLISHED_MATTERMOST = QUEUE_PREFIX + "ride_message_published_mattermost";
    String TRIP_MESSAGE_PUBLISHED_NOTIFICATION = QUEUE_PREFIX + "trip_message_published_notification";
    String TRIP_MESSAGE_PUBLISHED_MATTERMOST = QUEUE_PREFIX + "trip_message_published_mattermost";
    String RIDE_PUBLISHED_NOTIFICATION = QUEUE_PREFIX + "ride_published_notification";
    String RIDE_PUBLISHED_MAIL = QUEUE_PREFIX + "ride_published_mail";
    String RIDE_PUBLISHED_MATTERMOST = QUEUE_PREFIX + "ride_published_mattermost";
    String TRIP_PUBLISHED_MAIL = QUEUE_PREFIX + "trip_published_mail";
    String TRIP_PUBLISHED_NOTIFICATION = QUEUE_PREFIX + "trip_published_notification";
    String TRIP_PUBLISHED_MATTERMOST = QUEUE_PREFIX + "trip_published_mattermost";
    String TASK_PUBLISH_RIDES = QUEUE_PREFIX + RoutingKeys.TASK_PUBLISH_RIDES;
    String TASK_PUBLISH_TRIPS = QUEUE_PREFIX + RoutingKeys.TASK_PUBLISH_TRIPS;
    String TASK_PUBLISH_PUBLICATIONS = QUEUE_PREFIX + RoutingKeys.TASK_PUBLISH_PUBLICATIONS;
    String TASK_CLEAN_TMP_FILES = QUEUE_PREFIX + RoutingKeys.TASK_CLEAN_TMP_FILES;
    String TASK_CLEAN_TEAM_FILES = QUEUE_PREFIX + RoutingKeys.TASK_CLEAN_TEAM_FILES;
    String TASK_GENERATE_HEATMAPS = QUEUE_PREFIX + RoutingKeys.TASK_GENERATE_HEATMAPS;
    String TASK_GENERATE_HEATMAP = QUEUE_PREFIX + RoutingKeys.TASK_GENERATE_HEATMAP;
    String TASK_DOWNLOAD_PROFILE_IMAGE = QUEUE_PREFIX + RoutingKeys.TASK_DOWNLOAD_PROFILE_IMAGE;
    String TASK_CLEAN_NOTIFICATIONS = QUEUE_PREFIX + RoutingKeys.TASK_CLEAN_NOTIFICATIONS;
    String TASK_PERFORM_DELETION = QUEUE_PREFIX + RoutingKeys.TASK_PERFORM_DELETION;


}
