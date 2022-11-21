package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.domain.notification.Notification;
import info.tomacla.biketeam.domain.notification.NotificationRepository;
import info.tomacla.biketeam.domain.notification.NotificationType;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.service.broadcast.BroadcastService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService implements BroadcastService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRoleService userRoleService;

    public List<Notification> listUnviewedByUser(String userId) {
        return notificationRepository.findAllByUserIdAndViewedOrderByCreatedAtDesc(userId, false);
    }

    public Optional<Notification> getNotification(String id) {
        return notificationRepository.findById(id);
    }

    public void save(Notification notification) {
        notificationRepository.save(notification);
    }

    public void delete(String id) {
        getNotification(id).ifPresent(notificationRepository::delete);
    }

    public void deleteByUser(String userId) {
        notificationRepository.deleteByUserId(userId);
    }

    public void deleteByElement(String elementId) {
        notificationRepository.deleteByElementId(elementId);
    }

    @Override
    public boolean isConfigured(Team team) {
        return true;
    }

    @Override
    public void broadcast(Team team, Ride ride) {
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
    }

    @Override
    public void broadcast(Team team, Publication publication) {
        // ignore
    }

    @Override
    public void broadcast(Team team, Trip trip) {
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
    }

    @RabbitListener(queues = Queues.TASK_CLEAN_NOTIFICATIONS)
    public void cleanOldNotifications() {
        notificationRepository.deleteOld();
        notificationRepository.deleteOldRead();
    }


    public void markAllViewedForUser(String userId) {
        listUnviewedByUser(userId).forEach(notification -> {
            notification.setViewed(true);
            save(notification);
        });
    }
}
