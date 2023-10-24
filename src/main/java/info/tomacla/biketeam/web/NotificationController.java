package info.tomacla.biketeam.web;

import info.tomacla.biketeam.domain.notification.Notification;
import info.tomacla.biketeam.domain.notification.NotificationType;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.NotificationService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.TripService;
import info.tomacla.biketeam.service.url.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Controller
@RequestMapping(value = "/notifications")
public class NotificationController extends AbstractController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private RideService rideService;

    @Autowired
    private TripService tripService;

    @GetMapping(value = "/read-all")
    public String readAll(@RequestHeader(HttpHeaders.REFERER) String referer,
                          Principal principal,
                          Model model) {

        getUserFromPrincipal(principal).ifPresent(user -> notificationService.markAllViewedForUser(user));

        return "redirect:" + referer;

    }

    @GetMapping(value = "/{notificationId}")
    public String readOne(@PathVariable("notificationId") String notificationId,
                          @RequestHeader(HttpHeaders.REFERER) String referer,
                          Principal principal,
                          Model model) {

        AtomicReference<String> redirectUrl = new AtomicReference<>(referer);

        Optional<User> optionalUser = getUserFromPrincipal(principal);
        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            Optional<Notification> optionalNotification = notificationService.getNotification(notificationId);

            if (optionalNotification.isPresent()) {

                Notification notification = optionalNotification.get();

                if (notification.getUser().equals(user)) {

                    notification.setViewed(true);
                    notificationService.save(notification);

                    Team team = checkTeam(notification.getTeamId());

                    if (notification.getType().equals(NotificationType.RIDE_PUBLISHED)) {
                        rideService.get(team.getId(), notification.getElementId()).ifPresent(ride -> redirectUrl.set(viewHandler.redirect(team, "/rides/" + ride.getId())));
                    } else if (notification.getType().equals(NotificationType.TRIP_PUBLISHED)) {
                        tripService.get(team.getId(), notification.getElementId()).ifPresent(trip -> redirectUrl.set(viewHandler.redirect(team, "/trips/" + trip.getId())));
                    } else if (notification.getType().equals(NotificationType.NEW_RIDE_MESSAGE)) {
                        rideService.get(team.getId(), notification.getElementId()).ifPresent(ride -> redirectUrl.set(viewHandler.redirect(team, "/rides/" + ride.getId() + "/messages")));
                    } else if (notification.getType().equals(NotificationType.NEW_TRIP_MESSAGE)) {
                        tripService.get(team.getId(), notification.getElementId()).ifPresent(trip -> redirectUrl.set(viewHandler.redirect(team, "/trips/" + trip.getId() + "/trips")));
                    }

                }

            }

        }

        return redirectUrl.get();

    }

}
