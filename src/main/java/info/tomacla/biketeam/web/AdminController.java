package info.tomacla.biketeam.web;

import com.fasterxml.jackson.core.type.TypeReference;
import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.Gpx;
import info.tomacla.biketeam.common.Json;
import info.tomacla.biketeam.common.Point;
import info.tomacla.biketeam.domain.global.SiteConfiguration;
import info.tomacla.biketeam.domain.global.SiteDescription;
import info.tomacla.biketeam.domain.global.SiteIntegration;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.publication.PublicationRepository;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.ride.RideType;
import info.tomacla.biketeam.domain.user.UserRepository;
import info.tomacla.biketeam.service.FileService;
import info.tomacla.biketeam.web.forms.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.security.Principal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/admin")
public class AdminController extends AbstractController {

    @Autowired
    private FileService fileService;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private MapRepository mapRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String getSiteDescription(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Description");
        model.addAttribute("formdata", EditSiteDescriptionForm.build(siteDescriptionRepository.findById(1L).get()));
        return "admin_description";
    }

    @PostMapping
    public String updateSiteDescription(Principal principal, Model model,
                                        EditSiteDescriptionForm form) {

        try {
            @SuppressWarnings("OptionalGetWithoutIsPresent") SiteDescription siteDescription = siteDescriptionRepository.findById(1L).get();
            siteDescription.setSitename(form.getSitename());
            siteDescription.setDescription(form.getDescription());
            siteDescription.setFacebook(form.getFacebook());
            siteDescription.setTwitter(form.getTwitter());
            siteDescription.setEmail(form.getEmail());
            siteDescription.setPhoneNumber(form.getPhoneNumber());
            siteDescription.setAddressStreetLine(form.getAddressStreetLine());
            siteDescription.setAddressPostalCode(form.getAddressPostalCode());
            siteDescription.setAddressCity(form.getAddressCity());
            siteDescription.setOther(form.getOther());
            siteDescriptionRepository.save(siteDescription);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        } finally {
            model.addAttribute("formdata", EditSiteDescriptionForm.build(siteDescriptionRepository.findById(1L).get()));
            addGlobalValues(principal, model, "Administration - Description");
        }

        return "admin_description";

    }

    @GetMapping(value = "/configuration")
    public String getSiteConfiguration(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Configuration");
        model.addAttribute("formdata", EditSiteConfigurationForm.build(siteConfigurationRepository.findById(1L).get()));
        model.addAttribute("timezones", ZoneId.getAvailableZoneIds().stream().map(ZoneId::of).map(ZoneId::toString).sorted().collect(Collectors.toList()));
        return "admin_configuration";
    }

    @PostMapping(value = "/configuration")
    public String updateSiteConfiguration(Principal principal, Model model,
                                          EditSiteConfigurationForm form) {

        try {
            @SuppressWarnings("OptionalGetWithoutIsPresent") SiteConfiguration siteConfiguration = siteConfigurationRepository.findById(1L).get();
            siteConfiguration.setSoundEnabled(form.isSoundEnabled());
            siteConfiguration.setTimezone(form.getTimezone());
            siteConfigurationRepository.save(siteConfiguration);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        } finally {
            model.addAttribute("formdata", EditSiteConfigurationForm.build(siteConfigurationRepository.findById(1L).get()));
            model.addAttribute("timezones", ZoneId.getAvailableZoneIds().stream().map(ZoneId::of).map(ZoneId::toString).sorted().collect(Collectors.toList()));
            addGlobalValues(principal, model, "Administration - Configuration");
        }

        return "admin_configuration";

    }

    @GetMapping(value = "/integration")
    public String getSiteIntegration(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Intégrations");
        model.addAttribute("formdata", EditSiteIntegrationForm.build(siteIntegrationRepository.findById(1L).get()));
        return "admin_integration";
    }

    @PostMapping(value = "/integration")
    public String updateSiteIntegration(Principal principal, Model model,
                                        EditSiteIntegrationForm form) {

        try {
            @SuppressWarnings("OptionalGetWithoutIsPresent") SiteIntegration siteIntegration = siteIntegrationRepository.findById(1L).get();
            siteIntegration.setMapBoxAPIKey(form.getMapBoxAPIKey());
            siteIntegrationRepository.save(siteIntegration);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        } finally {
            model.addAttribute("formdata", EditSiteIntegrationForm.build(siteIntegrationRepository.findById(1L).get()));
            addGlobalValues(principal, model, "Administration - Intégrations");
        }

        return "admin_integration";

    }

    @GetMapping(value = "/logo")
    public String getLogo(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Logo");
        return "admin_logo";
    }

    @PostMapping(value = "/logo")
    public String updateLogo(Principal principal, Model model,
                             @RequestParam("file") MultipartFile file) {
        try {
            Path newLogo = fileService.getTempFileFromInputStream(file.getInputStream());
            fileService.store(newLogo, "logo.jpg");
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        } finally {
            addGlobalValues(principal, model, "Administration - Général");
        }

        return "admin_logo";

    }

    @GetMapping(value = "/publications")
    public String getPublications(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Publications");
        model.addAttribute("publications", publicationRepository.findAllByOrderByPostedAtDesc());
        return "admin_publications";
    }

    @PostMapping(value = "/publications")
    public String createPublication(Principal principal, Model model,
                                    NewPublicationForm form) {

        try {


            ZonedDateTime publishedAt = parseZonedDateTime(form.getPublishedAtDate(), form.getPublishedAtTime());

            Publication newPublication = new Publication(form.getTitle(), form.getContent(), publishedAt, form.fileSet());
            if (form.fileSet()) {
                Path tmp = fileService.getTempFileFromInputStream(form.getFile().getInputStream());
                fileService.store(tmp, FileRepositories.PUBLICATION_IMAGES, newPublication.getId() + ".jpg");
            }
            publicationRepository.save(newPublication);

            addGlobalValues(principal, model, "Administration - Publications");
            model.addAttribute("publications", publicationRepository.findAllByOrderByPostedAtDesc());
            return "admin_publications";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Nouvelle publication");
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "admin_publications_new";

        }

    }

    @PostMapping(value = "/publications/{publicationId}")
    public String editPublication(@PathVariable("publicationId") String publicationId,
                                  Principal principal,
                                  Model model,
                                  NewPublicationForm form) {

        try {

            Optional<Publication> optionalPublication = publicationRepository.findById(publicationId);
            if (optionalPublication.isEmpty()) {
                return "redirect:/admin/publications";
            }

            Publication publication = optionalPublication.get();
            publication.setContent(form.getContent());
            publication.setTitle(form.getTitle());

            if (form.fileSet()) {
                publication.setImaged(true);
                Path newImage = fileService.getTempFileFromInputStream(form.getFile().getInputStream());
                fileService.store(newImage, FileRepositories.PUBLICATION_IMAGES, publication.getId() + ".jpg");
            }

            publicationRepository.save(publication);

            return "redirect:/admin/publications";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Modifier la publication");
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "admin_publications_new";
        }

    }

    @GetMapping(value = "/publications/new")
    public String newPublication(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Nouvelle publication");
        model.addAttribute("formdata", NewPublicationForm.empty());
        return "admin_publications_new";
    }

    @GetMapping(value = "/publications/{publicationId}")
    public String editPublication(@PathVariable("publicationId") String publicationId,
                                  Principal principal,
                                  Model model) {

        Optional<Publication> optionalPublication = publicationRepository.findById(publicationId);
        if (optionalPublication.isEmpty()) {
            return "redirect:/admin/publications";
        }

        addGlobalValues(principal, model, "Administration - Modifier la publication");
        model.addAttribute("formdata", NewPublicationForm.build(optionalPublication.get()));
        return "admin_publications_new";
    }

    @GetMapping(value = "/publications/delete/{publicationId}")
    public String deletePublication(@PathVariable("publicationId") String publicationId,
                                    Model model) {

        try {
            publicationRepository.findById(publicationId).ifPresent(publication -> publicationRepository.delete(publication));

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/publications";
    }


    @GetMapping(value = "/rides")
    public String getRides(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Rides");
        model.addAttribute("rides", rideRepository.findAllByOrderByDateDesc());
        return "admin_rides";
    }

    @PostMapping(value = "/rides/{rideId}")
    public String createRide(@PathVariable("rideId") String rideId,
                             Principal principal,
                             Model model,
                             NewRideForm form) {

        try {

            if (!rideId.equals("new")) {
                Optional<Ride> optionalRide = rideRepository.findById(rideId);
                if (optionalRide.isEmpty()) {
                    return "redirect:/admin/rides";
                }
            }

            @SuppressWarnings("Convert2Diamond")
            List<NewRideForm.NewRideGroupForm> parsedGroups = Json.parse(form.getGroups(), new TypeReference<List<NewRideForm.NewRideGroupForm>>() {
            });
            Set<String> groupIdsSet = parsedGroups.stream().map(NewRideForm.NewRideGroupForm::getId).collect(Collectors.toSet());

            ZonedDateTime publishedAt = parseZonedDateTime(form.getPublishedAtDate(), form.getPublishedAtTime());

            Ride newRide;
            if (rideId.equals("new")) {
                newRide = new Ride(RideType.valueOf(form.getType()),
                        LocalDate.parse(form.getDate()),
                        publishedAt,
                        form.getTitle(), form.getDescription(),
                        form.fileSet(),
                        parsedGroups.stream()
                                .map(g -> new RideGroup(g.getName(), g.getLowerSpeed(),
                                        g.getUpperSpeed(),
                                        mapRepository.findById(g.getMapId()).isPresent() ? g.getMapId() : null,
                                        g.getMeetingLocation(),
                                        LocalTime.parse(g.getMeetingTime()),
                                        (g.getMeetingPoint() != null && !g.getMeetingPoint().isBlank()) ? Json.parse(g.getMeetingPoint(), Point.class) : null,
                                        null)
                                )
                                .collect(Collectors.toSet()));
            } else {
                newRide = rideRepository.findById(rideId).get();
                newRide.setType(RideType.valueOf(form.getType()));
                newRide.setDate(LocalDate.parse(form.getDate()));
                newRide.setTitle(form.getTitle());
                newRide.setDescription(form.getDescription());
                newRide.setPublishedAt(publishedAt);

                // remove groups not in list
                newRide.getGroups().removeIf(g -> !groupIdsSet.contains(g.getId()));
                // update and groups in list
                parsedGroups.forEach(g -> {
                    if (g.getId() == null || g.getId().equals("null")) { // FIXME
                        newRide.addGroup(new RideGroup(g.getName(), g.getLowerSpeed(),
                                g.getUpperSpeed(),
                                mapRepository.findById(g.getMapId()).isPresent() ? g.getMapId() : null,
                                g.getMeetingLocation(),
                                LocalTime.parse(g.getMeetingTime()),
                                (g.getMeetingPoint() != null && !g.getMeetingPoint().isBlank()) ? Json.parse(g.getMeetingPoint(), Point.class) : null,
                                null));
                    } else {
                        RideGroup toModify = newRide.getGroups().stream().filter(eg -> eg.getId().equals(g.getId())).findFirst().get();
                        toModify.setLowerSpeed(g.getLowerSpeed());
                        toModify.setUpperSpeed(g.getUpperSpeed());
                        toModify.setMapId(mapRepository.findById(g.getMapId()).isPresent() ? g.getMapId() : null);
                        toModify.setMeetingTime(LocalTime.parse(g.getMeetingTime()));
                        toModify.setMeetingLocation(g.getMeetingLocation());
                        toModify.setMeetingPoint((g.getMeetingPoint() != null && !g.getMeetingPoint().isBlank()) ? Json.parse(g.getMeetingPoint(), Point.class) : null);
                        toModify.setName(g.getName());
                    }
                });

            }

            if (form.fileSet()) {
                newRide.setImaged(true);
                Path tmp = fileService.getTempFileFromInputStream(form.getFile().getInputStream());
                fileService.store(tmp, FileRepositories.RIDE_IMAGES, newRide.getId() + ".jpg");
            }

            rideRepository.save(newRide);

            addGlobalValues(principal, model, "Administration - Rides");
            model.addAttribute("rides", rideRepository.findAllByOrderByDateDesc());
            return "admin_rides";


        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Nouveau ride");
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("maps", mapRepository.findAllByOrderByPostedAtDesc());
            model.addAttribute("formdata", form);
            return "admin_rides_new";
        }

    }

    @GetMapping(value = "/rides/{rideId}")
    public String editRide(@PathVariable("rideId") String rideId,
                           Principal principal,
                           Model model) {

        Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if (optionalRide.isEmpty()) {
            return "redirect:/admin/rides";
        }

        addGlobalValues(principal, model, "Administration - Modifier le ride");
        model.addAttribute("formdata", NewRideForm.build(optionalRide.get()));
        return "admin_rides_new";

    }

    @GetMapping(value = "/rides/new")
    public String newRide(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Nouveau ride");
        model.addAttribute("formdata", NewRideForm.empty());
        return "admin_rides_new";
    }

    @GetMapping(value = "/rides/delete/{rideId}")
    public String deleteRide(@PathVariable("rideId") String rideId,
                             Model model) {

        try {
            rideRepository.findById(rideId).ifPresent(ride -> rideRepository.delete(ride));

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/rides";

    }


    @GetMapping(value = "/maps")
    public String getMaps(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Maps");
        model.addAttribute("maps", mapRepository.findAllByOrderByPostedAtDesc());
        return "admin_maps";
    }

    @PostMapping(value = "/maps/{mapId}")
    public String editMap(@PathVariable("mapId") String mapId,
                          Principal principal,
                          Model model,
                          NewMapForm form) {

        try {

            Optional<Map> optionalMap = mapRepository.findById(mapId);
            if (optionalMap.isEmpty()) {
                return "redirect:/admin/maps";
            }

            @SuppressWarnings("Convert2Diamond")
            List<String> tags = Json.parse(form.getTags(), new TypeReference<List<String>>() {
            });

            Map map = optionalMap.get();
            map.setName(form.getName());
            map.setVisible(form.isVisible());
            map.setTags(tags);

            mapRepository.save(map);

            return "redirect:/admin/maps";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Modifier la map");
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "admin_maps_new";
        }

    }

    @GetMapping(value = "/maps/{mapId}")
    public String editMap(@PathVariable("mapId") String mapId,
                          Principal principal,
                          Model model) {


        Optional<Map> optionalMap = mapRepository.findById(mapId);
        if (optionalMap.isEmpty()) {
            return "redirect:/admin/maps";
        }

        addGlobalValues(principal, model, "Administration - Modifier la map");
        model.addAttribute("formdata", NewMapForm.build(optionalMap.get()));
        return "admin_maps_new";

    }

    @PostMapping(value = "/maps/new")
    public String newMapGpx(Model model,
                            Principal principal,
                            @RequestParam("file") MultipartFile file) {


        try {

            Path gpx = fileService.getTempFileFromInputStream(file.getInputStream());

            Gpx.GpxDescriptor gpxParsed = Gpx.parse(gpx);
            Path staticMapImage = Gpx.staticImage(gpx, siteIntegrationRepository.findById(1L).get().getMapBoxAPIKey());

            Map newMap = new Map(file.getOriginalFilename(),
                    gpxParsed.getLength(),
                    gpxParsed.getPositiveElevation(),
                    gpxParsed.getNegativeElevation(),
                    new ArrayList<>(),
                    gpxParsed.getStart(),
                    gpxParsed.getEnd(),
                    gpxParsed.getWind(),
                    gpxParsed.isCrossing(),
                    false);

            fileService.store(gpx, FileRepositories.GPX_FILES, newMap.getId() + ".gpx");
            fileService.store(staticMapImage, FileRepositories.MAP_IMAGES, newMap.getId() + ".png");

            mapRepository.save(newMap);

            return "redirect:/admin/maps/" + newMap.getId();


        } catch (Exception e) {

            addGlobalValues(principal, model, "Administration - Maps");
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("maps", mapRepository.findAllByOrderByPostedAtDesc());
            return "admin_maps";

        }

    }

    @GetMapping(value = "/maps/delete/{mapId}")
    public String deleteMap(@PathVariable("mapId") String mapId,
                            Model model) {

        try {
            mapRepository.findById(mapId).ifPresent(map -> mapRepository.delete(map));

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/maps";
    }

    @GetMapping(value = "/users")
    public String getUsers(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Utilisateurs");
        model.addAttribute("users", userRepository.findAll());
        return "admin_users";
    }

    @GetMapping(value = "/users/promote/{userId}")
    public String promoteUser(@PathVariable("userId") String userId,
                              Model model) {

        try {
            userRepository.findById(userId).ifPresent(user -> {
                user.setAdmin(true);
                userRepository.save(user);
            });

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/users";
    }

    @GetMapping(value = "/users/relegate/{userId}")
    public String relagetUser(@PathVariable("userId") String userId,
                              Model model) {

        try {
            userRepository.findById(userId).ifPresent(user -> {
                user.setAdmin(false);
                userRepository.save(user);
            });

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/users";
    }

    private ZonedDateTime parseZonedDateTime(String datePart, String timePart) {
        ZoneId timezone = ZoneId.of(siteConfigurationRepository.findById(1L).get().getTimezone());
        return ZonedDateTime.of(LocalDateTime.parse(datePart + "T" + timePart), timezone);
    }

}
