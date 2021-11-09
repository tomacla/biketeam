package info.tomacla.biketeam.web.team.ride;

import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.template.RideTemplate;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.RideTemplateService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/admin/rides")
public class AdminTeamRideController extends AbstractController {

    @Autowired
    private RideService rideService;

    @Autowired
    private MapService mapService;

    @Autowired
    private RideTemplateService rideTemplateService;


    @GetMapping
    public String getRides(@PathVariable("teamId") String teamId, Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "Administration - Rides", team);
        model.addAttribute("rides", rideService.listRides(team.getId()));
        model.addAttribute("templates", rideTemplateService.listTemplates(team.getId()));
        return "team_admin_rides";
    }

    @GetMapping(value = "/new")
    public String newRide(@PathVariable("teamId") String teamId,
                          @RequestParam(value = "templateId", required = false, defaultValue = "empty-1") String templateId,
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
        model.addAttribute("published", false);
        return "team_admin_rides_new";

    }

    @GetMapping(value = "/{rideId}")
    public String editRide(@PathVariable("teamId") String teamId,
                           @PathVariable("rideId") String rideId,
                           Principal principal,
                           Model model) {

        final Team team = checkTeam(teamId);

        Optional<Ride> optionalRide = rideService.get(team.getId(), rideId);
        if (optionalRide.isEmpty()) {
            return redirectToAdminRides(team);
        }

        Ride ride = optionalRide.get();

        NewRideForm form = NewRideForm.builder(ride.getGroups().size(), ZonedDateTime.now(), team.getZoneId())
                .withId(ride.getId())
                .withDate(ride.getDate())
                .withDescription(ride.getDescription())
                .withType(ride.getType())
                .withPublishedAt(ride.getPublishedAt(), team.getZoneId())
                .withTitle(ride.getTitle())
                .withGroups(ride.getSortedGroups(), team.getId(), mapService)
                .get();

        addGlobalValues(principal, model, "Administration - Modifier le ride", team);
        model.addAttribute("formdata", form);
        model.addAttribute("published", ride.getPublishedStatus().equals(PublishedStatus.PUBLISHED));
        return "team_admin_rides_new";

    }

    @PostMapping(value = "/{rideId}")
    public String editRide(@PathVariable("teamId") String teamId,
                           @PathVariable("rideId") String rideId,
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
                    return redirectToAdminRides(team);
                }
                target = optionalRide.get();
                target.setDate(parser.getDate());
                if (target.getPublishedStatus().equals(PublishedStatus.UNPUBLISHED)) {
                    // do not change published date if already published
                    target.setPublishedAt(parser.getPublishedAt(timezone));
                }
                target.setTitle(parser.getTitle());
                target.setDescription(parser.getDescription());
                target.setType(parser.getType());

                target.addOrReplaceGroups(parser.getGroups(teamId, mapService));

            } else {
                target = new Ride(team.getId(), parser.getType(), parser.getDate(), parser.getPublishedAt(timezone),
                        parser.getTitle(), parser.getDescription(), parser.getFile().isPresent(), null);
                if (parser.getTemplateId() != null) {
                    rideTemplateService.increment(team.getId(), parser.getTemplateId());
                }
                // new group so just add all groups
                parser.getGroups(team.getId(), mapService).forEach(target::addGroup);
            }

            if (parser.getFile().isPresent()) {
                target.setImaged(true);
                MultipartFile uploadedFile = parser.getFile().get();
                rideService.saveImage(team.getId(), target.getId(), form.getFile().getInputStream(), uploadedFile.getOriginalFilename());
            }

            rideService.save(target);

            addGlobalValues(principal, model, "Administration - Rides", team);
            model.addAttribute("rides", rideService.listRides(team.getId()));
            model.addAttribute("templates", rideTemplateService.listTemplates(team.getId()));
            return "team_admin_rides";


        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Modifier le ride", team);
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "team_admin_rides_new";
        }

    }


    @GetMapping(value = "/delete/{rideId}")
    public String deleteRide(@PathVariable("teamId") String teamId,
                             @PathVariable("rideId") String rideId,
                             Principal principal,
                             Model model) {

        final Team team = checkTeam(teamId);

        try {
            rideService.delete(team.getId(), rideId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return redirectToAdminRides(team);

    }

    private String redirectToAdminRides(Team team) {
        return createRedirect(team, "/admin/rides");
    }

}
