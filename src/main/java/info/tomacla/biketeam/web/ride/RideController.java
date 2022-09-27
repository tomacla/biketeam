package info.tomacla.biketeam.web.ride;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.message.RideMessage;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.MessageService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.UserRoleService;
import info.tomacla.biketeam.service.file.ThumbnailService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/rides")
public class RideController extends AbstractController {

    @Autowired
    private RideService rideService;

    @Autowired
    private MapService mapService;

    @Autowired
    private ThumbnailService thumbnailService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private MessageService messageService;

    @GetMapping(value = "/{rideId}")
    public String getRide(@PathVariable("teamId") String teamId,
                          @PathVariable("rideId") String rideId,
                          @ModelAttribute("error") String error,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        Optional<Ride> optionalRide = rideService.get(team.getId(), rideId);
        if (optionalRide.isEmpty()) {
            return viewHandler.redirect(team, "/rides");
        }

        Ride ride = optionalRide.get();

        if (!ride.getPublishedStatus().equals(PublishedStatus.PUBLISHED) && !isAdmin(principal, team)) {
            return viewHandler.redirect(team, "/rides");
        }

        addGlobalValues(principal, model, "Ride " + ride.getTitle(), team);
        model.addAttribute("ride", ride);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "ride";
    }

    @GetMapping(value = "/{rideId}/groups")
    public String getRideGroups(@PathVariable("teamId") String teamId,
                                @PathVariable("rideId") String rideId,
                                @ModelAttribute("error") String error,
                                Principal principal,
                                Model model) {

        final Team team = checkTeam(teamId);

        Optional<Ride> optionalRide = rideService.get(team.getId(), rideId);
        if (optionalRide.isEmpty()) {
            return viewHandler.redirect(team, "/rides");
        }

        Ride ride = optionalRide.get();

        if (!ride.getPublishedStatus().equals(PublishedStatus.PUBLISHED) && !isAdmin(principal, team)) {
            return viewHandler.redirect(team, "/rides");
        }

        addGlobalValues(principal, model, "Ride " + ride.getTitle(), team);
        model.addAttribute("ride", ride);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "ride_groups";
    }

    @GetMapping(value = "/{rideId}/messages")
    public String getRideMessages(@PathVariable("teamId") String teamId,
                                  @PathVariable("rideId") String rideId,
                                  @ModelAttribute("error") String error,
                                  Principal principal,
                                  Model model) {

        final Team team = checkTeam(teamId);

        Optional<Ride> optionalRide = rideService.get(team.getId(), rideId);
        if (optionalRide.isEmpty()) {
            return viewHandler.redirect(team, "/rides");
        }

        Ride ride = optionalRide.get();

        if (!ride.getPublishedStatus().equals(PublishedStatus.PUBLISHED) && !isAdmin(principal, team)) {
            return viewHandler.redirect(team, "/rides");
        }

        addGlobalValues(principal, model, "Ride " + ride.getTitle(), team);
        model.addAttribute("ride", ride);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }

        return "ride_messages";
    }

    @GetMapping
    public String getRides(@PathVariable("teamId") String teamId,
                           @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                           @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                           @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                           @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
                           @ModelAttribute("error") String error,
                           Principal principal,
                           Model model) {

        final Team team = checkTeam(teamId);

        SearchRideForm form = SearchRideForm.builder()
                .withFrom(from)
                .withTo(to)
                .withPage(page)
                .withPageSize(pageSize)
                .get();

        final SearchRideForm.SearchRideFormParser parser = form.parser();

        Page<Ride> rides = rideService.searchRides(
                team.getId(),
                parser.getPage(),
                parser.getPageSize(),
                parser.getFrom(),
                parser.getTo()
        );

        addGlobalValues(principal, model, "Rides", team);
        model.addAttribute("rides", rides.getContent());
        model.addAttribute("pages", rides.getTotalPages());
        model.addAttribute("formdata", form);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "rides";

    }

    @GetMapping(value = "/{rideId}/add-participant/{groupId}")
    public RedirectView addParticipantToRide(@PathVariable("teamId") String teamId,
                                             @PathVariable("rideId") String rideId,
                                             @PathVariable("groupId") String groupId,
                                             RedirectAttributes attributes,
                                             Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        try {
            Optional<Ride> optionalRide = rideService.get(team.getId(), rideId);
            if (optionalRide.isEmpty()) {
                return viewHandler.redirectView(team, "/rides");
            }

            Ride ride = optionalRide.get();
            Optional<RideGroup> optionalGroup = ride.getGroups().stream().filter(rg -> rg.getId().equals(groupId)).findFirst();
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

            if (optionalConnectedUser.isPresent() && optionalGroup.isPresent()) {
                RideGroup rideGroup = optionalGroup.get();
                User connectedUser = optionalConnectedUser.get();

                if (!team.isMember(connectedUser)) {
                    final UserRole userRole = new UserRole(team, connectedUser, Role.MEMBER);
                    userRoleService.save(userRole);
                }

                if (!ride.hasParticipant(connectedUser.getId())) {
                    rideGroup.addParticipant(connectedUser);
                    rideService.save(ride);
                }

            }

            return viewHandler.redirectView(team, "/rides/" + rideId + "/groups");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/rides/" + rideId + "/groups");
        }
    }

    @GetMapping(value = "/{rideId}/remove-participant/{groupId}")
    public RedirectView removeParticipantFromRide(@PathVariable("teamId") String teamId,
                                                  @PathVariable("rideId") String rideId,
                                                  @PathVariable("groupId") String groupId,
                                                  RedirectAttributes attributes,
                                                  Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        try {
            Optional<Ride> optionalRide = rideService.get(team.getId(), rideId);
            if (optionalRide.isEmpty()) {
                return viewHandler.redirectView(team, "/rides");
            }

            Ride ride = optionalRide.get();
            Optional<RideGroup> optionalGroup = ride.getGroups().stream().filter(rg -> rg.getId().equals(groupId)).findFirst();
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

            if (optionalConnectedUser.isPresent() && optionalGroup.isPresent()) {
                RideGroup rideGroup = optionalGroup.get();
                User connectedUser = optionalConnectedUser.get();

                rideGroup.removeParticipant(connectedUser);
                rideService.save(ride);

            }

            return viewHandler.redirectView(team, "/rides/" + rideId + "/groups");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/rides/" + rideId + "/groups");
        }
    }

    @PostMapping(value = "/{rideId}/add-message")
    public RedirectView addMessage(@PathVariable("teamId") String teamId,
                                   @PathVariable("rideId") String rideId,
                                   @RequestParam("content") String content,
                                   RedirectAttributes attributes,
                                   Principal principal,
                                   Model model) {

        final Team team = checkTeam(teamId);

        try {

            Optional<Ride> optionalRide = rideService.get(team.getId(), rideId);
            if (optionalRide.isEmpty()) {
                return viewHandler.redirectView(team, "/rides");
            }

            Ride ride = optionalRide.get();
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

            if (optionalConnectedUser.isPresent()) {

                User connectedUser = optionalConnectedUser.get();

                RideMessage rideMessage = new RideMessage();
                rideMessage.setRide(ride);
                rideMessage.setUser(connectedUser);
                rideMessage.setContent(content);

                messageService.save(team, rideMessage);

            }

            return viewHandler.redirectView(team, "/rides/" + rideId + "/messages");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/rides/" + rideId + "/messages");
        }
    }

    @GetMapping(value = "/{rideId}/remove-message/{messageId}")
    public RedirectView removeMessage(@PathVariable("teamId") String teamId,
                                      @PathVariable("rideId") String rideId,
                                      @PathVariable("messageId") String messageId,
                                      RedirectAttributes attributes,
                                      Principal principal,
                                      Model model) {

        final Team team = checkTeam(teamId);

        try {

            Optional<Ride> optionalRide = rideService.get(team.getId(), rideId);
            if (optionalRide.isEmpty()) {
                return viewHandler.redirectView(team, "/rides");
            }

            Ride ride = optionalRide.get();
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
            final Optional<RideMessage> optionalMessage = messageService.getRideMessage(messageId);

            if (optionalConnectedUser.isPresent() && optionalMessage.isPresent()) {

                User connectedUser = optionalConnectedUser.get();
                final RideMessage message = optionalMessage.get();

                if (message.getRide().equals(ride) && (message.getUser().equals(connectedUser) || team.isAdmin(connectedUser))) {
                    ride.removeMessage(messageId);
                    messageService.deleteRideMessage(messageId);
                }

            }

            return viewHandler.redirectView(team, "/rides/" + rideId + "/messages");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/rides/" + rideId + "/messages");
        }
    }

    @ResponseBody
    @RequestMapping(value = "/{rideId}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getRideImage(@PathVariable("teamId") String teamId,
                                               @PathVariable("rideId") String rideId,
                                               @RequestParam(name = "width", defaultValue = "-1", required = false) int targetWidth) {

        final Optional<ImageDescriptor> image = rideService.getImage(teamId, rideId);
        if (image.isPresent()) {
            try {

                final ImageDescriptor targetImage = image.get();
                final FileExtension targetImageExtension = targetImage.getExtension();

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", targetImageExtension.getMediaType());
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(rideId + targetImageExtension.getExtension())
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
                throw new ServerErrorException("Error while reading ride image : " + rideId, e);
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find ride image : " + rideId);

    }


}
