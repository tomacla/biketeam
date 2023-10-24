package info.tomacla.biketeam.web.team.ride;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.template.RideTemplate;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.PlaceService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.RideTemplateService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/{teamId}/admin/rides")
public class AdminTeamRideController extends AbstractController {

    @Autowired
    private RideService rideService;

    @Autowired
    private MapService mapService;

    @Autowired
    private PlaceService placeService;

    @Autowired
    private RideTemplateService rideTemplateService;

    @GetMapping
    public String getRides(@PathVariable("teamId") String teamId,
                           @ModelAttribute("error") String error,
                           @RequestParam(value = "title", defaultValue = "", required = false) String title,
                           @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                           @RequestParam(value = "pageSize", defaultValue = "20", required = false) int pageSize,
                           Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "Administration - Rides", team);
        Page<Ride> rides = rideService.listRides(team.getId(), title, page, pageSize);
        model.addAttribute("rides", rides.getContent());
        model.addAttribute("templates", rideTemplateService.listTemplates(team.getId()));
        model.addAttribute("matches", rides.getTotalElements());
        model.addAttribute("pages", rides.getTotalPages());
        model.addAttribute("page", page);
        model.addAttribute("title", title);
        model.addAttribute("pageSize", pageSize);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_rides";

    }

    @GetMapping(value = "/new")
    public String newRide(@PathVariable("teamId") String teamId,
                          @RequestParam(value = "templateId", required = false, defaultValue = "empty-1") String templateId,
                          @ModelAttribute("error") String error,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        NewRideForm form = null;

        if (templateId != null && !templateId.startsWith("empty-")) {
            Optional<RideTemplate> optionalTemplate = rideTemplateService.get(team.getId(), templateId);
            if (optionalTemplate.isPresent()) {
                form = NewRideForm.builder(optionalTemplate.get(), ZonedDateTime.now(), team.getZoneId()).get();
            }
        }

        if (form == null && templateId.startsWith("empty-")) {
            int numberOfGroups = Integer.parseInt(templateId.replace("empty-", ""));
            form = NewRideForm.builder(numberOfGroups, ZonedDateTime.now(), team.getZoneId()).get();
        }

        if (form == null) {
            form = NewRideForm.builder(1, ZonedDateTime.now(), team.getZoneId()).get();
        }

        addGlobalValues(principal, model, "Administration - Nouveau ride", team);
        model.addAttribute("formdata", form);
        model.addAttribute("startPlaces", placeService.listPlaces(teamId).stream().filter(p -> p.isStartPlace()).collect(Collectors.toList()));
        model.addAttribute("endPlaces", placeService.listPlaces(teamId).stream().filter(p -> p.isEndPlace()).collect(Collectors.toList()));
        model.addAttribute("imaged", false);
        model.addAttribute("published", false);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_rides_new";

    }

    @GetMapping(value = "/{rideId}")
    public String editRide(@PathVariable("teamId") String teamId,
                           @PathVariable("rideId") String rideId,
                           @ModelAttribute("error") String error,
                           Principal principal,
                           Model model) {

        final Team team = checkTeam(teamId);

        Optional<Ride> optionalRide = rideService.get(team.getId(), rideId);
        if (optionalRide.isEmpty()) {
            return viewHandler.redirect(team, "/admin/rides");
        }

        Ride ride = optionalRide.get();

        NewRideForm form = NewRideForm.builder(ride.getGroups().size(), ZonedDateTime.now(), team.getZoneId())
                .withId(ride.getId())
                .withPermalink(ride.getPermalink())
                .withDate(ride.getDate())
                .withDescription(ride.getDescription())
                .withType(ride.getType())
                .withPublishedAt(ride.getPublishedAt(), team.getZoneId())
                .withListedInFeed(ride.isListedInFeed())
                .withTitle(ride.getTitle())
                .withGroups(ride.getSortedGroups())
                .withStartPlace(ride.getStartPlace())
                .withEndPlace(ride.getEndPlace())
                .get();

        addGlobalValues(principal, model, "Administration - Modifier le ride", team);
        model.addAttribute("formdata", form);
        model.addAttribute("startPlaces", placeService.listPlaces(teamId).stream().filter(p -> p.isStartPlace()).collect(Collectors.toList()));
        model.addAttribute("endPlaces", placeService.listPlaces(teamId).stream().filter(p -> p.isEndPlace()).collect(Collectors.toList()));
        model.addAttribute("imaged", ride.isImaged());
        model.addAttribute("published", ride.getPublishedStatus().equals(PublishedStatus.PUBLISHED));
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_rides_new";

    }

    @PostMapping(value = "/{rideId}")
    public RedirectView editRide(@PathVariable("teamId") String teamId,
                                 @PathVariable("rideId") String rideId,
                                 RedirectAttributes attributes,
                                 Principal principal,
                                 Model model,
                                 NewRideForm form) {

        final Team team = checkTeam(teamId);

        try {

            boolean isNew = rideId.equals("new");
            final ZoneId timezone = team.getZoneId();

            NewRideForm.NewRideFormParser parser = form.parser();
            Ride target;
            if (!isNew) {
                Optional<Ride> optionalRide = rideService.get(team.getId(), rideId);
                if (optionalRide.isEmpty()) {
                    return viewHandler.redirectView(team, "/admin/rides");
                }
                target = optionalRide.get();
                if (target.getPublishedStatus().equals(PublishedStatus.UNPUBLISHED)) {
                    // do not change published date if already published
                    target.setPublishedAt(parser.getPublishedAt(timezone));
                }

                target.addOrReplaceGroups(parser.getGroups(teamId, mapService));

            } else {

                target = new Ride();
                target.setTeamId(team.getId());

                target.setPublishedAt(parser.getPublishedAt(timezone));

                if (parser.getTemplateId() != null) {
                    rideTemplateService.increment(team.getId(), parser.getTemplateId());
                }
                // new group so just add all groups
                parser.getGroups(team.getId(), mapService).forEach(target::addGroup);

            }

            target.setListedInFeed(parser.isListedInFeed());
            target.setDescription(parser.getDescription());
            target.setTitle(parser.getTitle());
            target.setPermalink(parser.getPermalink());
            target.setType(parser.getType());
            target.setDate(parser.getDate());
            target.setStartPlace(parser.getStartPlace(teamId, placeService));
            target.setEndPlace(parser.getEndPlace(teamId, placeService));

            if (parser.getFile().isPresent()) {
                target.setImaged(true);
                MultipartFile uploadedFile = parser.getFile().get();
                rideService.saveImage(team.getId(), target.getId(), form.getFile().getInputStream(), uploadedFile.getOriginalFilename());
            }

            rideService.save(target);

            return viewHandler.redirectView(team, "/admin/rides");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/rides/" + rideId);
        }

    }


    @GetMapping(value = "/delete/{rideId}")
    public RedirectView deleteRide(@PathVariable("teamId") String teamId,
                                   @PathVariable("rideId") String rideId,
                                   RedirectAttributes attributes,
                                   Principal principal,
                                   Model model) {

        final Team team = checkTeam(teamId);

        try {
            rideService.delete(team.getId(), rideId);
            return viewHandler.redirectView(team, "/admin/rides");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/rides");
        }


    }

}
