package info.tomacla.biketeam.web;

import info.tomacla.biketeam.domain.notification.Notification;
import info.tomacla.biketeam.domain.notification.NotificationType;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping(value = "/notifications")
public class NotificationController extends AbstractController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping(value = "/read-all")
    public String readAll(@RequestHeader(HttpHeaders.REFERER) String referer,
                          Principal principal,
                          Model model) {

        getUserFromPrincipal(principal).ifPresent(user -> notificationService.markAllViewedForUser(user.getId()));

        return "redirect:" + referer;

    }

    @GetMapping(value = "/{notificationId}")
    public ModelAndView readOne(@PathVariable("notificationId") String notificationId,
                                @RequestHeader(HttpHeaders.REFERER) String referer,
                                Principal principal,
                                Model model) {

        Optional<User> optionalUser = getUserFromPrincipal(principal);
        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            Optional<Notification> optionalNotification = notificationService.getNotification(notificationId);

            if (optionalNotification.isPresent()) {

                Notification notification = optionalNotification.get();

                if(notification.getUser().equals(user)) {

                    notification.setViewed(true);
                    notificationService.save(notification);

                    Team team = checkTeam(notification.getTeamId());

                    if (notification.getType().equals(NotificationType.RIDE_PUBLISHED)) {
                        return new ModelAndView(viewHandler.redirectView(team, "/rides/" + notification.getElementId()));
                    } else if(notification.getType().equals(NotificationType.TRIP_PUBLISHED)) {
                        return new ModelAndView(viewHandler.redirectView(team, "/trips/" + notification.getElementId()));
                    } else if (notification.getType().equals(NotificationType.NEW_RIDE_MESSAGE)) {
                        return new ModelAndView(viewHandler.redirectView(team, "/rides/" + notification.getElementId() + "/messages"));
                    } else if(notification.getType().equals(NotificationType.NEW_TRIP_MESSAGE)) {
                        return new ModelAndView(viewHandler.redirectView(team, "/trips/" + notification.getElementId() + "/messages"));
                    }

                }

            }

        }

        return new ModelAndView("redirect:" + referer);

    }

}
