package info.tomacla.biketeam.web.trip;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.message.Message;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.MessageService;
import info.tomacla.biketeam.service.TripService;
import info.tomacla.biketeam.service.UserRoleService;
import info.tomacla.biketeam.service.file.ThumbnailService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/trips")
public class TripController extends AbstractController {

    @Autowired
    private TripService tripService;

    @Autowired
    private MapService mapService;

    @Autowired
    private ThumbnailService thumbnailService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private MessageService messageService;

    @GetMapping(value = "/{tripId}")
    public String getTrip(@PathVariable("teamId") String teamId,
                          @PathVariable("tripId") String tripId,
                          @ModelAttribute("error") String error,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
        if (optionalTrip.isEmpty()) {
            return viewHandler.redirect(team, "/trips");
        }

        Trip trip = optionalTrip.get();

        if (!trip.getPublishedStatus().equals(PublishedStatus.PUBLISHED) && !isAdmin(principal, team)) {
            return viewHandler.redirect(team, "/trips");
        }

        addGlobalValues(principal, model, "Trip " + trip.getTitle(), team);
        model.addAttribute("trip", trip);
        model.addAttribute("messages", messageService.listByTarget(trip));
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "trip";
    }

    @GetMapping(value = "/{tripId}/reactions")
    public String getReactionFragment(@PathVariable("teamId") String teamId,
                                      @PathVariable("tripId") String tripId,
                                      @ModelAttribute("error") String error,
                                      Principal principal,
                                      Model model) {

        final Team team = checkTeam(teamId);

        Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
        if (optionalTrip.isEmpty()) {
            return viewHandler.redirect(team, "/");
        }

        Trip trip = optionalTrip.get();

        if (!trip.getPublishedStatus().equals(PublishedStatus.PUBLISHED) && !isAdmin(principal, team)) {
            return viewHandler.redirect(team, "/");
        }

        addGlobalValues(principal, model, "Trip " + trip.getTitle(), team);
        model.addAttribute("urlPartPrefix", "trips");
        model.addAttribute("element", trip);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "_fragment_reactions";
    }

    @GetMapping(value = "/{tripId}/messages")
    public String getTripMessages(@PathVariable("teamId") String teamId,
                                  @PathVariable("tripId") String tripId,
                                  @ModelAttribute("error") String error,
                                  Principal principal,
                                  Model model) {

        final Team team = checkTeam(teamId);

        Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
        if (optionalTrip.isEmpty()) {
            return viewHandler.redirect(team, "/trips");
        }

        Trip trip = optionalTrip.get();

        if (!trip.getPublishedStatus().equals(PublishedStatus.PUBLISHED) && !isAdmin(principal, team)) {
            return viewHandler.redirect(team, "/trips");
        }

        addGlobalValues(principal, model, "Trip " + trip.getTitle(), team);
        model.addAttribute("trip", trip);
        model.addAttribute("messages", messageService.listByTarget(trip));
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "trip_messages";
    }


    @GetMapping
    public RedirectView getTrips(@PathVariable("teamId") String teamId,
                                 Principal principal,
                                 Model model) {

        final Team team = checkTeam(teamId);

        return viewHandler.redirectView(team, "/");

    }

    @GetMapping(value = "/{tripId}/add-participant")
    public RedirectView addParticipantToTrip(@PathVariable("teamId") String teamId,
                                             @PathVariable("tripId") String tripId,
                                             RedirectAttributes attributes,
                                             Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        try {
            Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
            if (optionalTrip.isEmpty()) {
                return viewHandler.redirectView(team, "/trips");
            }

            Trip trip = optionalTrip.get();
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

            if (optionalConnectedUser.isPresent()) {
                User connectedUser = optionalConnectedUser.get();

                if (!team.isMember(connectedUser)) {
                    userRoleService.save(new UserRole(team, connectedUser, Role.MEMBER));
                }

                if (!trip.hasParticipant(connectedUser.getId())) {
                    trip.addParticipant(connectedUser);
                    tripService.save(trip);
                }

            }

            return viewHandler.redirectView(team, "/trips/" + tripId);

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/trips/" + tripId);
        }
    }

    @GetMapping(value = "/{tripId}/remove-participant")
    public RedirectView removeParticipantToTrip(@PathVariable("teamId") String teamId,
                                                @PathVariable("tripId") String tripId,
                                                RedirectAttributes attributes,
                                                Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        try {
            Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
            if (optionalTrip.isEmpty()) {
                return viewHandler.redirectView(team, "/trips");
            }

            Trip trip = optionalTrip.get();
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

            if (optionalConnectedUser.isPresent()) {
                User connectedUser = optionalConnectedUser.get();
                trip.removeParticipant(connectedUser);
                tripService.save(trip);

            }

            return viewHandler.redirectView(team, "/trips/" + tripId);

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/trips/" + tripId);
        }
    }

    @PostMapping(value = "/{tripId}/add-message")
    public RedirectView addMessage(@PathVariable("teamId") String teamId,
                                   @PathVariable("tripId") String tripId,
                                   @RequestParam("content") String content,
                                   @RequestParam("replyToId") String replyToId,
                                   @RequestParam("originId") String originId,
                                   RedirectAttributes attributes,
                                   Principal principal,
                                   Model model) {

        final Team team = checkTeam(teamId);

        try {

            Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
            if (optionalTrip.isEmpty()) {
                return viewHandler.redirectView(team, "/trips");
            }

            Trip trip = optionalTrip.get();
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

            Message message;
            if (!ObjectUtils.isEmpty(originId)) {
                Optional<Message> optionalMessage = messageService.getMessage(originId);
                if (optionalMessage.isEmpty()) {
                    return viewHandler.redirectView(team, "/trips/" + tripId + "/messages");
                }

                message = optionalMessage.get();

            } else {

                if (optionalConnectedUser.isEmpty()) {
                    return viewHandler.redirectView(team, "/trips/" + tripId + "/messages");
                }

                User connectedUser = optionalConnectedUser.get();
                message = new Message();
                message.setTarget(trip);
                message.setUser(connectedUser);
                if (replyToId != null) {
                    Optional<Message> optionalReplyMessage = messageService.getMessage(replyToId);
                    if (optionalReplyMessage.isPresent() && optionalReplyMessage.get().getTargetId().equals(trip.getId())) {
                        message.setReplyToId(replyToId);
                    }
                }

            }

            message.setContent(content);

            messageService.save(team, trip, message);

            return viewHandler.redirectView(team, "/trips/" + tripId + "/messages");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/trips/" + tripId + "/messages");
        }
    }

    @GetMapping(value = "/{tripId}/remove-message/{messageId}")
    public RedirectView removeMessage(@PathVariable("teamId") String teamId,
                                      @PathVariable("tripId") String tripId,
                                      @PathVariable("messageId") String messageId,
                                      RedirectAttributes attributes,
                                      Principal principal,
                                      Model model) {

        final Team team = checkTeam(teamId);

        try {

            Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
            if (optionalTrip.isEmpty()) {
                return viewHandler.redirectView(team, "/trips");
            }

            Trip trip = optionalTrip.get();
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
            final Optional<Message> optionalMessage = messageService.getMessage(messageId);

            if (optionalConnectedUser.isPresent() && optionalMessage.isPresent()) {


                User connectedUser = optionalConnectedUser.get();
                final Message message = optionalMessage.get();

                if (message.getTargetId().equals(trip.getId()) && (connectedUser.isAdmin() || message.getUser().equals(connectedUser) || team.isAdmin(connectedUser))) {
                    messageService.delete(messageId);
                }

            }

            return viewHandler.redirectView(team, "/trips/" + tripId + "/messages");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/trips/" + tripId + "/messages");
        }
    }

    @ResponseBody
    @RequestMapping(value = "/{tripId}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getTripImage(@PathVariable("teamId") String teamId,
                                               @PathVariable("tripId") String tripId,
                                               @RequestParam(name = "width", defaultValue = "-1", required = false) int targetWidth) {

        final Optional<ImageDescriptor> image = tripService.getImage(teamId, tripId);
        if (image.isPresent()) {
            try {

                final ImageDescriptor targetImage = image.get();
                final FileExtension targetImageExtension = targetImage.getExtension();

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", targetImageExtension.getMediaType());
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(tripId + targetImageExtension.getExtension())
                        .build());

                byte[] bytes = Files.readAllBytes(targetImage.getPath());
                if (targetWidth != -1) {
                    bytes = thumbnailService.resizeImage(bytes, targetWidth, targetImageExtension);
                }

                return new ResponseEntity<>(
                        bytes,
                        headers,
                        HttpStatus.OK
                );

            } catch (IOException e) {
                throw new ServerErrorException("Error while reading trip image : " + tripId, e);
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find trip image : " + tripId);

    }


}
