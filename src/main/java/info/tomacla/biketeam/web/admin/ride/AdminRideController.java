package info.tomacla.biketeam.web.admin.ride;

import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.domain.global.RideTemplate;
import info.tomacla.biketeam.domain.global.RideTemplateRepository;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.service.FileService;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/admin/rides")
public class AdminRideController extends AbstractController {

    @Autowired
    private FileService fileService;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private MapService mapService;

    @Autowired
    private RideTemplateRepository rideTemplateRepository;


    @GetMapping
    public String getRides(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Rides");
        model.addAttribute("rides", rideRepository.findAllByOrderByDateDesc());
        model.addAttribute("templates", rideTemplateRepository.findAllByOrderByNameAsc());
        return "admin_rides";
    }

    @GetMapping(value = "/new")
    public String newRide(@RequestParam(value = "templateId", required = false, defaultValue = "empty-1") String templateId,
                          Principal principal,
                          Model model) {

        NewRideForm form = null;

        if (templateId != null && !templateId.startsWith("empty-")) {
            Optional<RideTemplate> optionalTemplate = rideTemplateRepository.findById(templateId);
            if (optionalTemplate.isPresent()) {
                form = NewRideForm.builder(optionalTemplate.get()).get();
            }
        }

        if (form == null && templateId.startsWith("empty-")) {
            int numberOfGroups = Integer.parseInt(templateId.replace("empty-", ""));
            form = NewRideForm.builder(numberOfGroups).get();
        }

        if (form == null) {
            form = NewRideForm.builder(1).get();
        }

        addGlobalValues(principal, model, "Administration - Nouveau ride");
        model.addAttribute("formdata", form);
        model.addAttribute("published", false);
        return "admin_rides_new";

    }

    @GetMapping(value = "/{rideId}")
    public String editRide(@PathVariable("rideId") String rideId,
                           Principal principal,
                           Model model) {

        Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if (optionalRide.isEmpty()) {
            return "redirect:/admin/rides";
        }

        Ride ride = optionalRide.get();

        NewRideForm form = NewRideForm.builder(ride.getGroups().size())
                .withId(ride.getId())
                .withDate(ride.getDate())
                .withDescription(ride.getDescription())
                .withType(ride.getType())
                .withPublishedAt(ride.getPublishedAt())
                .withTitle(ride.getTitle())
                .withGroups(ride.getGroups(), mapService)
                .get();

        addGlobalValues(principal, model, "Administration - Modifier le ride");
        model.addAttribute("formdata", form);
        model.addAttribute("published", ride.getPublishedStatus().equals(PublishedStatus.PUBLISHED));
        return "admin_rides_new";

    }

    @PostMapping(value = "/{rideId}")
    public String editRide(@PathVariable("rideId") String rideId,
                           Principal principal,
                           Model model,
                           NewRideForm form) {

        try {

            boolean isNew = rideId.equals("new");

            NewRideForm.NewRideFormParser parser = form.parser();
            Ride target;
            if (!isNew) {
                Optional<Ride> optionalRide = rideRepository.findById(rideId);
                if (optionalRide.isEmpty()) {
                    return "redirect:/admin/rides";
                }
                target = optionalRide.get();
                target.setDate(parser.getDate());
                target.setPublishedAt(parser.getPublishedAt(configurationService.getTimezone()));
                target.setTitle(parser.getTitle());
                target.setDescription(parser.getDescription());
                target.setType(parser.getType());
            } else {
                target = new Ride(parser.getType(), parser.getDate(), parser.getPublishedAt(configurationService.getTimezone()),
                        parser.getTitle(), parser.getDescription(), parser.getFile().isPresent());
            }

            Set<RideGroup> groups = parser.getGroups(target, mapService);
            Set<String> groupIdsSet = groups.stream().map(RideGroup::getId).collect(Collectors.toSet());

            // remove groups not in list
            target.getGroups().removeIf(g -> !groupIdsSet.contains(g.getId()));
            // update and groups in list
            for (RideGroup g : groups) {
                Optional<RideGroup> optionalGroup = target.getGroups().stream().filter(eg -> eg.getId().equals(g.getId())).findFirst();
                if (optionalGroup.isEmpty()) {
                    target.addGroup(new RideGroup(g.getName(),
                            g.getLowerSpeed(),
                            g.getUpperSpeed(),
                            g.getMapId(),
                            g.getMeetingLocation(),
                            g.getMeetingTime(),
                            g.getMeetingPoint(),
                            new HashSet<>()));

                    if (parser.getFile().isPresent()) {
                        target.setImaged(true);
                    }
                } else {
                    RideGroup toModify = optionalGroup.get();
                    toModify.setMapId(g.getMapId());
                    toModify.setLowerSpeed(g.getLowerSpeed());
                    toModify.setUpperSpeed(g.getUpperSpeed());
                    toModify.setMeetingTime(g.getMeetingTime());
                    toModify.setMeetingLocation(g.getMeetingLocation());
                    toModify.setMeetingPoint(g.getMeetingPoint());
                    toModify.setName(g.getName());
                }
            }

            if (parser.getFile().isPresent()) {
                MultipartFile uploadedFile = parser.getFile().get();
                Optional<FileExtension> optionalFileExtension = FileExtension.findByFileName(uploadedFile.getOriginalFilename());
                if (optionalFileExtension.isPresent()) {
                    Path newImage = fileService.getTempFileFromInputStream(form.getFile().getInputStream());
                    fileService.store(newImage, FileRepositories.RIDE_IMAGES, target.getId() + optionalFileExtension.get().getExtension());
                }
            }

            rideRepository.save(target);

            addGlobalValues(principal, model, "Administration - Rides");
            model.addAttribute("rides", rideRepository.findAllByOrderByDateDesc());
            model.addAttribute("templates", rideTemplateRepository.findAllByOrderByNameAsc());
            return "admin_rides";


        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Modifier le ride");
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "admin_rides_new";
        }

    }


    @GetMapping(value = "/delete/{rideId}")
    public String deleteRide(@PathVariable("rideId") String rideId,
                             Model model) {

        try {
            rideRepository.findById(rideId).ifPresent(ride -> rideRepository.delete(ride));

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/rides";

    }


}
