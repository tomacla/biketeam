package info.tomacla.biketeam.common.amqp;

public interface RoutingKeys {

    String TASK_PUBLISH_RIDES = "task_publish_rides";
    String TASK_PUBLISH_TRIPS = "task_publish_trips";
    String TASK_PUBLISH_PUBLICATIONS = "task_publish_publications";
    String TASK_CLEAN_TMP_FILES = "task_clean_tmp_files";
    String TASK_CLEAN_TEAM_FILES = "task_clean_team_files";
    String TASK_PERFORM_DELETION = "task_perform_deletion";
    String TASK_DOWNLOAD_PROFILE_IMAGE = "task_download_profile_image";
    String TASK_CLEAN_NOTIFICATIONS = "task_clean_notifications";


}
