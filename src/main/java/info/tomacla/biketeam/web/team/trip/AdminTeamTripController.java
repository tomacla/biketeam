package info.tomacla.biketeam.web.team.trip;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.place.PlaceSorterOption;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.PlaceService;
import info.tomacla.biketeam.service.TripService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

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

    @Autowired
    private PlaceService placeService;

    @GetMapping
    public String getTrips(@PathVariable("teamId") String teamId,
                           @ModelAttribute("error") String error,
                           Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "Administration - Trips", team);
        model.addAttribute("trips", tripService.listTrips(team.getId()));
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_trips";

    }

    @GetMapping(value = "/new")
    public String newTrip(@PathVariable("teamId") String teamId,
                          @ModelAttribute("error") String error,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        NewTripForm form = NewTripForm.builder(ZonedDateTime.now(), team.getZoneId()).get();

        addGlobalValues(principal, model, "Administration - Nouveau trip", team);
        model.addAttribute("formdata", form);
        model.addAttribute("startPlaces", placeService.listPlacesWithAppearances(teamId, PlaceSorterOption.TRIP_START));
        model.addAttribute("endPlaces", placeService.listPlacesWithAppearances(teamId, PlaceSorterOption.TRIP_END));
        model.addAttribute("imaged", false);
        model.addAttribute("published", false);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_trips_new";

    }

    @GetMapping(value = "/{tripId}")
    public String editTrip(@PathVariable("teamId") String teamId,
                           @PathVariable("tripId") String tripId,
                           @ModelAttribute("error") String error,
                           Principal principal,
                           Model model) {

        final Team team = checkTeam(teamId);

        Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
        if (optionalTrip.isEmpty()) {
            return viewHandler.redirect(team, "/admin/trips");
        }

        Trip trip = optionalTrip.get();

        NewTripForm form = NewTripForm.builder(ZonedDateTime.now(), team.getZoneId())
                .withId(trip.getId())
                .withPermalink(trip.getPermalink())
                .withStartDate(trip.getStartDate())
                .withEndDate(trip.getEndDate())
                .withDescription(trip.getDescription())
                .withType(trip.getType())
                .withPublishedAt(trip.getPublishedAt(), team.getZoneId())
                .withTitle(trip.getTitle())
                .withMeetingTime(trip.getMeetingTime())
                .withStages(trip.getSortedStages())
                .withStartPlace(trip.getStartPlace())
                .withEndPlace(trip.getEndPlace())
                .get();

        addGlobalValues(principal, model, "Administration - Modifier le trip", team);
        model.addAttribute("formdata", form);
        model.addAttribute("startPlaces", placeService.listPlacesWithAppearances(teamId, PlaceSorterOption.TRIP_START));
        model.addAttribute("endPlaces", placeService.listPlacesWithAppearances(teamId, PlaceSorterOption.TRIP_END));
        model.addAttribute("imaged", trip.isImaged());
        model.addAttribute("published", trip.getPublishedStatus().equals(PublishedStatus.PUBLISHED));
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_trips_new";

    }

    @PostMapping(value = "/{tripId}")
    public RedirectView editTrip(@PathVariable("teamId") String teamId,
                                 @PathVariable("tripId") String tripId,
                                 RedirectAttributes attributes,
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
                    return viewHandler.redirectView(team, "/admin/trips");
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
                target.setMeetingTime(parser.getMeetingTime());
                target.setPermalink(parser.getPermalink());

                target.addOrReplaceStages(parser.getStages(teamId, mapService));

            } else {
                target = new Trip();
                target.setTeamId(team.getId());
                target.setType(parser.getType());
                target.setStartDate(parser.getStartDate());
                target.setEndDate(parser.getEndDate());
                target.setPublishedAt(parser.getPublishedAt(timezone));
                target.setTitle(parser.getTitle());
                target.setDescription(parser.getDescription());
                target.setImaged(parser.getFile().isPresent());
                target.setMeetingTime(parser.getMeetingTime());
                target.setPermalink(parser.getPermalink());

                // new group so just add all groups
                parser.getStages(team.getId(), mapService).forEach(target::addStage);
            }

            target.setStartPlace(parser.getStartPlace(teamId, placeService));
            target.setEndPlace(parser.getEndPlace(teamId, placeService));

            if (parser.getFile().isPresent()) {
                target.setImaged(true);
                MultipartFile uploadedFile = parser.getFile().get();
                tripService.saveImage(team.getId(), target.getId(), form.getFile().getInputStream(), uploadedFile.getOriginalFilename());
            }

            tripService.save(target);

            return viewHandler.redirectView(team, "/admin/trips");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/trips/" + tripId);
        }

    }


    @GetMapping(value = "/delete/{tripId}")
    public RedirectView deleteTrip(@PathVariable("teamId") String teamId,
                                   @PathVariable("tripId") String tripId,
                                   RedirectAttributes attributes,
                                   Principal principal,
                                   Model model) {

        final Team team = checkTeam(teamId);

        try {
            tripService.delete(team.getId(), tripId);
            return viewHandler.redirectView(team, "/admin/trips");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/trips");
        }

    }

}
