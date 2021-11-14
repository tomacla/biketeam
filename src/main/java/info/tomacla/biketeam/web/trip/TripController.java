package info.tomacla.biketeam.web.trip;

import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.common.ImageDescriptor;
import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripStage;
import info.tomacla.biketeam.domain.user.Role;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.ThumbnailService;
import info.tomacla.biketeam.service.TripService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/{teamId}/trips")
public class TripController extends AbstractController {

    @Autowired
    private TripService tripService;

    @Autowired
    private MapService mapService;

    @Autowired
    private ThumbnailService thumbnailService;

    @GetMapping(value = "/{tripId}")
    public String getTrip(@PathVariable("teamId") String teamId,
                          @PathVariable("tripId") String tripId,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
        if (optionalTrip.isEmpty()) {
            return redirectToTrips(team);
        }

        Trip trip = optionalTrip.get();

        if (!trip.getPublishedStatus().equals(PublishedStatus.PUBLISHED) && !isAdmin(principal, team)) {
            return redirectToTrips(team);
        }

        final java.util.Map<String, Map> maps = trip.getStages().stream()
                .map(TripStage::getMapId)
                .filter(Objects::nonNull)
                .distinct()
                .map(mapId -> mapService.get(teamId, mapId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Map::getId, m -> m));


        addGlobalValues(principal, model, "Trip " + trip.getTitle(), team);
        model.addAttribute("trip", trip);
        model.addAttribute("maps", maps);
        return "trip";
    }

    @GetMapping
    public String getTrips(@PathVariable("teamId") String teamId,
                           @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                           @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                           @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                           @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
                           Principal principal,
                           Model model) {

        final Team team = checkTeam(teamId);

        SearchTripForm form = SearchTripForm.builder()
                .withFrom(from)
                .withTo(to)
                .withPage(page)
                .withPageSize(pageSize)
                .get();

        final SearchTripForm.SearchTripFormParser parser = form.parser();

        Page<Trip> trips = tripService.searchTrips(
                team.getId(),
                parser.getPage(),
                parser.getPageSize(),
                parser.getFrom(),
                parser.getTo()
        );

        addGlobalValues(principal, model, "Trips", team);
        model.addAttribute("trips", trips.getContent());
        model.addAttribute("pages", trips.getTotalPages());
        model.addAttribute("formdata", form);
        return "trips";

    }

    @GetMapping(value = "/{tripId}/add-participant")
    public String addParticipantToTrip(@PathVariable("teamId") String teamId,
                                       @PathVariable("tripId") String tripId,
                                       Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
        if (optionalTrip.isEmpty()) {
            return redirectToTrips(team);
        }

        Trip trip = optionalTrip.get();
        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isPresent()) {
            User connectedUser = optionalConnectedUser.get();

            if (!team.isMember(connectedUser.getId())) {
                team.addRole(connectedUser, Role.MEMBER);
                teamService.save(team);
            }

            if (!trip.hasParticipant(connectedUser.getId())) {
                trip.addParticipant(connectedUser);
                tripService.save(trip);
            }

        }

        return redirectToTrip(team, tripId);
    }

    @GetMapping(value = "/{tripId}/remove-participant")
    public String removeParticipantToTrip(@PathVariable("teamId") String teamId,
                                          @PathVariable("tripId") String tripId,
                                          Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
        if (optionalTrip.isEmpty()) {
            return redirectToTrips(team);
        }

        Trip trip = optionalTrip.get();
        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isPresent()) {
            User connectedUser = optionalConnectedUser.get();
            trip.removeParticipant(connectedUser);
            tripService.save(trip);

        }

        return redirectToTrip(team, tripId);
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
