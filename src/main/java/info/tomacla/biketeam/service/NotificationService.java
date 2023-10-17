package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.domain.message.Message;
import info.tomacla.biketeam.domain.notification.Notification;
import info.tomacla.biketeam.domain.notification.NotificationRepository;
import info.tomacla.biketeam.domain.notification.NotificationType;
import info.tomacla.biketeam.domain.notification.SearchNotificationSpecification;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.service.amqp.dto.TeamEntityDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private RideService rideService;

    @Autowired
    private TripService tripService;

    @Autowired
    private MessageService messageService;

    public List<Notification> listUnviewedByUser(User user) {
        return notificationRepository.findAll(SearchNotificationSpecification.unviewedByUser(user));
    }

    public Optional<Notification> getNotification(String id) {
        return notificationRepository.findById(id);
    }

    public void markAllViewedForUser(User user) {
        listUnviewedByUser(user).forEach(notification -> {
            notification.setViewed(true);
            save(notification);
        });
    }

    @Transactional
    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    @Transactional
    public void delete(String id) {
        getNotification(id).ifPresent(notificationRepository::delete);
    }

    @RabbitListener(queues = Queues.RIDE_PUBLISHED_NOTIFICATION)
    public void consumeRidePublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.RIDE_PUBLISHED_NOTIFICATION);
            teamService.get(body.teamId).ifPresent(team ->
                    rideService.get(body.teamId, body.id).ifPresent(ride -> {

                        for (UserRole role : team.getRoles()) {
                            Notification n = new Notification();
                            n.setTeamId(team.getId());
                            n.setElementId(ride.getId());
                            n.setUser(role.getUser());
                            n.setType(NotificationType.RIDE_PUBLISHED);
                            n.setViewed(false);
                            n.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
                            save(n);
                        }

                    })
            );

        } catch (Exception e) {
            log.error("Error in event " + Queues.RIDE_PUBLISHED_NOTIFICATION, e);
        }
    }

    @RabbitListener(queues = Queues.TRIP_PUBLISHED_NOTIFICATION)
    public void consumeTripPublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.TRIP_PUBLISHED_NOTIFICATION);
            teamService.get(body.teamId).ifPresent(team ->
                    tripService.get(body.teamId, body.id).ifPresent(trip -> {

                        for (UserRole role : team.getRoles()) {
                            Notification n = new Notification();
                            n.setTeamId(team.getId());
                            n.setElementId(trip.getId());
                            n.setUser(role.getUser());
                            n.setType(NotificationType.TRIP_PUBLISHED);
                            n.setViewed(false);
                            n.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
                            notificationRepository.save(n);
                        }

                    })
            );

        } catch (Exception e) {
            log.error("Error in event " + Queues.TRIP_PUBLISHED_NOTIFICATION, e);
        }
    }

    @RabbitListener(queues = Queues.RIDE_MESSAGE_PUBLISHED_NOTIFICATION)
    public void consumeRideMessagePublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.RIDE_MESSAGE_PUBLISHED_NOTIFICATION);
            teamService.get(body.teamId).ifPresent(team ->
                    messageService.getMessage(body.id).ifPresent(message -> {

                        final Message replyTo = message.getReplyToId() != null ? messageService.getMessage(message.getReplyToId()).orElse(null) : null;

                        team.getRoles().stream()
                                .filter(r -> ((replyTo != null && replyTo.getUser().equals(r.getUser())) || r.getRole().equals(Role.ADMIN)) && !r.getUser().getId().equals(message.getUser().getId()))
                                .forEach(role -> {

                                    Notification n = new Notification();
                                    n.setTeamId(team.getId());
                                    n.setElementId(message.getTargetId());
                                    n.setUser(role.getUser());
                                    n.setType(NotificationType.NEW_RIDE_MESSAGE);
                                    n.setViewed(false);
                                    n.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
                                    this.save(n);

                                });

                    })
            );

        } catch (Exception e) {
            log.error("Error in event " + Queues.RIDE_MESSAGE_PUBLISHED_NOTIFICATION, e);
        }
    }

    @RabbitListener(queues = Queues.TRIP_MESSAGE_PUBLISHED_NOTIFICATION)
    public void consumeTripMessagePublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.TRIP_MESSAGE_PUBLISHED_NOTIFICATION);
            teamService.get(body.teamId).ifPresent(team ->
                    messageService.getMessage(body.id).ifPresent(message -> {

                        final Message replyTo = message.getReplyToId() != null ? messageService.getMessage(message.getReplyToId()).orElse(null) : null;

                        team.getRoles().stream()
                                .filter(r -> ((replyTo != null && replyTo.getUser().equals(r.getUser())) || r.getRole().equals(Role.ADMIN)) && !r.getUser().getId().equals(message.getUser().getId()))
                                .forEach(role -> {

                                    Notification n = new Notification();
                                    n.setTeamId(team.getId());
                                    n.setElementId(message.getTargetId());
                                    n.setUser(role.getUser());
                                    n.setType(NotificationType.NEW_TRIP_MESSAGE);
                                    n.setViewed(false);
                                    n.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
                                    this.save(n);

                                });

                    })
            );

        } catch (Exception e) {
            log.error("Error in event " + Queues.TRIP_MESSAGE_PUBLISHED_NOTIFICATION, e);
        }
    }

    @RabbitListener(queues = Queues.TASK_CLEAN_NOTIFICATIONS)
    public void cleanOldNotifications() {
        try {
            notificationRepository.deleteOld();
        } catch (Exception e) {
            log.error("Error while cleaning notification", e);
        }
    }


}
