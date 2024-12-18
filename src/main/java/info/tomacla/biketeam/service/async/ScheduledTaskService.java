package info.tomacla.biketeam.service.async;

import info.tomacla.biketeam.common.amqp.Exchanges;
import info.tomacla.biketeam.common.amqp.RoutingKeys;
import info.tomacla.biketeam.service.amqp.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * For CRON expression, order to use is "second, minute, hour, day of month, month, day(s) of week"
 */
@Service
public class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    private BrokerService brokerService;

    @Scheduled(fixedRate = 60000, initialDelay = 10000)
    public void publicationTask() {
        log.debug("Executing scheduled publicationTask");
        brokerService.sendToBroker(Exchanges.TASK, RoutingKeys.TASK_PUBLISH_RIDES, null);
        brokerService.sendToBroker(Exchanges.TASK, RoutingKeys.TASK_PUBLISH_PUBLICATIONS, null);
        brokerService.sendToBroker(Exchanges.TASK, RoutingKeys.TASK_PUBLISH_TRIPS, null);
    }

    @Scheduled(cron = "0 30 3 * * 0")
    public void cleanTmpDirectory() {
        log.debug("Executing scheduled cleanTmpDirectory");
        brokerService.sendToBroker(Exchanges.TASK, RoutingKeys.TASK_CLEAN_TMP_FILES, null);
    }

    @Scheduled(cron = "0 0 3 * * 0")
    public void cleanDeleteTeamsDirectory() {
        log.debug("Executing scheduled cleanDeleteTeamsDirectory");
        brokerService.sendToBroker(Exchanges.TASK, RoutingKeys.TASK_CLEAN_TEAM_FILES, null);
    }

    @Scheduled(cron = "0 30 2 * * 0")
    public void cleanNotifications() {
        log.debug("Executing scheduled cleanNotifications");
        brokerService.sendToBroker(Exchanges.TASK, RoutingKeys.TASK_CLEAN_NOTIFICATIONS, null);
    }

    @Scheduled(fixedRate = 30000, initialDelay = 3000)
    public void performDeletion() {
        log.debug("Executing scheduled performDeletion");
        brokerService.sendToBroker(Exchanges.TASK, RoutingKeys.TASK_PERFORM_DELETION, null);
    }

}