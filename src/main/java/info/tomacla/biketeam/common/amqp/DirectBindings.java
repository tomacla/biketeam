package info.tomacla.biketeam.common.amqp;

public enum DirectBindings {

    TASK_GENERATE_HEATMAPS(Exchanges.TASK, Queues.TASK_GENERATE_HEATMAPS, RoutingKeys.TASK_GENERATE_HEATMAPS),
    TASK_PUBLISH_RIDES(Exchanges.TASK, Queues.TASK_PUBLISH_RIDES, RoutingKeys.TASK_PUBLISH_RIDES),
    TASK_PUBLISH_TRIPS(Exchanges.TASK, Queues.TASK_PUBLISH_TRIPS, RoutingKeys.TASK_PUBLISH_TRIPS),
    TASK_PUBLISH_PUBLICATIONS(Exchanges.TASK, Queues.TASK_PUBLISH_PUBLICATIONS, RoutingKeys.TASK_PUBLISH_PUBLICATIONS),
    TASK_CLEAN_TMP_FILES(Exchanges.TASK, Queues.TASK_CLEAN_TMP_FILES, RoutingKeys.TASK_CLEAN_TMP_FILES),
    TASK_CLEAN_TEAM_FILES(Exchanges.TASK, Queues.TASK_CLEAN_TEAM_FILES, RoutingKeys.TASK_CLEAN_TEAM_FILES),
    PUBLICATION_PUBLISHED(Exchanges.EVENT, Queues.PUBLICATION_PUBLISHED, RoutingKeys.PUBLICATION_PUBLISHED),
    RIDE_MESSAGE_PUBLISHED(Exchanges.EVENT, Queues.RIDE_MESSAGE_PUBLISHED, RoutingKeys.RIDE_MESSAGE_PUBLISHED),
    TRIP_MESSAGE_PUBLISHED(Exchanges.EVENT, Queues.TRIP_MESSAGE_PUBLISHED, RoutingKeys.TRIP_MESSAGE_PUBLISHED),
    RIDE_PUBLISHED(Exchanges.EVENT, Queues.RIDE_PUBLISHED, RoutingKeys.RIDE_PUBLISHED),
    TRIP_PUBLISHED(Exchanges.EVENT, Queues.TRIP_PUBLISHED, RoutingKeys.TRIP_PUBLISHED);

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
