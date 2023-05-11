package info.tomacla.biketeam.common.amqp;

public enum DirectBindings {

    TASK_CLEAN_NOTIFICATIONS(Exchanges.TASK, Queues.TASK_CLEAN_NOTIFICATIONS, RoutingKeys.TASK_CLEAN_NOTIFICATIONS),
    TASK_DOWNLOAD_PROFILE_IMAGE(Exchanges.TASK, Queues.TASK_DOWNLOAD_PROFILE_IMAGE, RoutingKeys.TASK_DOWNLOAD_PROFILE_IMAGE),
    TASK_GENERATE_HEATMAPS(Exchanges.TASK, Queues.TASK_GENERATE_HEATMAPS, RoutingKeys.TASK_GENERATE_HEATMAPS),
    TASK_PUBLISH_RIDES(Exchanges.TASK, Queues.TASK_PUBLISH_RIDES, RoutingKeys.TASK_PUBLISH_RIDES),
    TASK_PUBLISH_TRIPS(Exchanges.TASK, Queues.TASK_PUBLISH_TRIPS, RoutingKeys.TASK_PUBLISH_TRIPS),
    TASK_PUBLISH_PUBLICATIONS(Exchanges.TASK, Queues.TASK_PUBLISH_PUBLICATIONS, RoutingKeys.TASK_PUBLISH_PUBLICATIONS),
    TASK_CLEAN_TMP_FILES(Exchanges.TASK, Queues.TASK_CLEAN_TMP_FILES, RoutingKeys.TASK_CLEAN_TMP_FILES),
    TASK_CLEAN_TEAM_FILES(Exchanges.TASK, Queues.TASK_CLEAN_TEAM_FILES, RoutingKeys.TASK_CLEAN_TEAM_FILES),
    TASK_PERFORM_DELETION(Exchanges.TASK, Queues.TASK_PERFORM_DELETION, RoutingKeys.TASK_PERFORM_DELETION);

    final String exchange;
    final String queue;
    final String routingKey;

    DirectBindings(String exchange, String queue, String routingKey) {
        this.exchange = exchange;
        this.queue = queue;
        this.routingKey = routingKey;
    }

    public String getExchange() {
        return exchange;
    }

    public String getQueue() {
        return queue;
    }

    public String getRoutingKey() {
        return routingKey;
    }
}
