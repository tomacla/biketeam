package info.tomacla.biketeam.web.team.trip;

import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.TripService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/admin/trips")
public class AdminTeamTripController extends AbstractController {

    @Autowired
    private TripService tripService;

    @Autowired
    private MapService mapService;

    @GetMapping
    public String getTrips(@PathVariable("teamId") String teamId, Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "Administration - Trips", team);
        model.addAttribute("trips", tripService.listTrips(team.getId()));
        return "team_admin_trips";

    }

    @GetMapping(value = "/new")
    public String newTrip(@PathVariable("teamId") String teamId,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        NewTripForm form = NewTripForm.builder(ZonedDateTime.now(), team.getZoneId()).get();

        addGlobalValues(principal, model, "Administration - Nouveau trip", team);
        model.addAttribute("formdata", form);
        model.addAttribute("published", false);
        return "team_admin_trips_new";

    }

    @GetMapping(value = "/{tripId}")
    public String editTrip(@PathVariable("teamId") String teamId,
                           @PathVariable("tripId") String tripId,
                           Principal principal,
                           Model model) {

        final Team team = checkTeam(teamId);

        Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
        if (optionalTrip.isEmpty()) {
            return redirectToAdminTrips(team);
        }

        Trip trip = optionalTrip.get();

        NewTripForm form = NewTripForm.builder(ZonedDateTime.now(), team.getZoneId())
                .withId(trip.getId())
                .withStartDate(trip.getStartDate())
                .withEndDate(trip.getEndDate())
                .withDescription(trip.getDescription())
                .withType(trip.getType())
                .withPublishedAt(trip.getPublishedAt(), team.getZoneId())
                .withTitle(trip.getTitle())
                .withLowerSpeed(trip.getLowerSpeed())
                .withUpperSpeed(trip.getUpperSpeed())
                .withMeetingLocation(trip.getMeetingLocation())
                .withMeetingPoint(trip.getMeetingPoint())
                .withMeetingTime(trip.getMeetingTime())
                .withStages(trip.getSortedStages(), team.getId(), mapService)
                .get();

        addGlobalValues(principal, model, "Administration - Modifier le trip", team);
        model.addAttribute("formdata", form);
        model.addAttribute("published", trip.getPublishedStatus().equals(PublishedStatus.PUBLISHED));
        return "team_admin_trips_new";

    }

    @PostMapping(value = "/{tripId}")
    public String editTrip(@PathVariable("teamId") String teamId,
                           @PathVariable("tripId") String tripId,
                           Principal principal,
                           Model model,
                           NewTripForm form) {

        final Team team = checkTeam(teamId);

        try {

            boolean isNew = tripId.equals("new");
            final ZoneId timezone = team.getZoneId();

            final NewTripForm.NewTripFormParser parser = form.parser();
            Trip target;
            if (!isNew) {
                Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
                if (optionalTrip.isEmpty()) {
                    return redirectToAdminTrips(team);
                }
                target = optionalTrip.get();
                target.setStartDate(parser.getStartDate());
                target.setEndDate(parser.getEndDate());
                if (target.getPublishedStatus().equals(PublishedStatus.UNPUBLISHED)) {
                    // do not change published date if already published
                    target.setPublishedAt(parser.getPublishedAt(timezone));
                }
                target.setTitle(parser.getTitle());
                target.setDescription(parser.getDescription());
                target.setType(parser.getType());
                target.setLowerSpeed(parser.getLowerSpeed());
                target.setUpperSpeed(parser.getUpperSpeed());
                target.setMeetingPoint(parser.getMeetingPoint().orElse(null));
                target.setMeetingLocation(parser.getMeetingLocation());
                target.setMeetingTime(parser.getMeetingTime());

                target.addOrReplaceStages(parser.getStages(teamId, mapService));

            } else {
                target = new Trip(team.getId(), parser.getType(), parser.getStartDate(),
                        parser.getEndDate(), parser.getLowerSpeed(), parser.getUpperSpeed(), parser.getPublishedAt(timezone),
                        parser.getTitle(), parser.getDescription(), parser.getFile().isPresent(), parser.getMeetingLocation(), parser.getMeetingTime(),
                        parser.getMeetingPoint().orElse(null));
                // new group so just add all groups
                parser.getStages(team.getId(), mapService).forEach(target::addStage);
            }

            if (parser.getFile().isPresent()) {
                target.setImaged(true);
                MultipartFile uploadedFile = parser.getFile().get();
                tripService.saveImage(team.getId(), target.getId(), form.getFile().getInputStream(), uploadedFile.getOriginalFilename());
            }

            tripService.save(target);

            addGlobalValues(principal, model, "Administration - Trips", team);
            model.addAttribute("trips", tripService.listTrips(team.getId()));
            return "team_admin_trips";


        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Modifier le trip", team);
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "team_admin_trips_new";
        }

    }


    @GetMapping(value = "/delete/{tripId}")
    public String deleteTrip(@PathVariable("teamId") String teamId,
                             @PathVariable("tripId") String tripId,
                             Principal principal,
                             Model model) {

        final Team team = checkTeam(teamId);

        try {
            tripService.delete(team.getId(), tripId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return redirectToAdminTrips(team);

    }

    private String redirectToAdminTrips(Team team) {
        return createRedirect(team, "/admin/trips");
    }

}
