package info.tomacla.biketeam.web.ride;

import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

@Controller
@RequestMapping(value = "/rides")
public class RideController extends AbstractController {

    @Autowired
    private RideRepository rideRepository;

    @GetMapping(value = "/{rideId}")
    public String getRide(@PathVariable("rideId") String rideId,
                          Principal principal,
                          Model model) {

        Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if (optionalRide.isEmpty()) {
            return "redirect:/rides";
        }

        Ride ride = optionalRide.get();
        addGlobalValues(principal, model, "Ride " + ride.getTitle());
        model.addAttribute("ride", ride);
        return "ride";
    }

    @GetMapping
    public String getRides(@RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                           @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                           @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                           @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
                           Principal principal,
                           Model model) {

        SearchRideForm form = SearchRideForm.builder()
                .withFrom(from)
                .withTo(to)
                .withPage(page)
                .withPageSize(pageSize)
                .get();

        Page<Ride> rides = getRidesFromRepository(form);

        addGlobalValues(principal, model, "Rides");
        model.addAttribute("rides", rides.getContent());
        model.addAttribute("pages", rides.getTotalPages());
        model.addAttribute("formdata", form);
        return "rides";

    }

    @GetMapping(value = "/{rideId}/add-participant/{groupId}/{userId}")
    public String addParticipantToRide(@PathVariable("rideId") String rideId,
                                       @PathVariable("groupId") String groupId,
                                       @PathVariable("userId") String userId,
                                       Principal principal, Model model) {

        Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if (optionalRide.isEmpty()) {
            return "redirect:/rides";
        }

        Ride ride = optionalRide.get();
        Optional<RideGroup> optionalGroup = ride.getGroups().stream().filter(rg -> rg.getId().equals(groupId)).findFirst();
        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isPresent() && optionalGroup.isPresent() && optionalUser.isPresent()) {
            RideGroup rideGroup = optionalGroup.get();
            User user = optionalUser.get();
            User connectedUser = optionalConnectedUser.get();

            if (user.equals(connectedUser) || connectedUser.isAdmin()) {
                rideGroup.addParticipant(user);
                rideRepository.save(ride);
            }

        }

        return "redirect:/rides/" + rideId;
    }

    @GetMapping(value = "/{rideId}/remove-participant/{groupId}/{userId}")
    public String removeParticipantToRide(@PathVariable("rideId") String rideId,
                                          @PathVariable("groupId") String groupId,
                                          @PathVariable("userId") String userId,
                                          Principal principal, Model model) {

        Optional<Ride> optionalRide = rideRepository.findById(rideId);
        if (optionalRide.isEmpty()) {
            return "redirect:/rides";
        }

        Ride ride = optionalRide.get();
        Optional<RideGroup> optionalGroup = ride.getGroups().stream().filter(rg -> rg.getId().equals(groupId)).findFirst();
        Optional<User> optionalUser = userRepository.findById(userId);
        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isPresent() && optionalGroup.isPresent() && optionalUser.isPresent()) {
            RideGroup rideGroup = optionalGroup.get();
            User user = optionalUser.get();
            User connectedUser = optionalConnectedUser.get();

            if (user.equals(connectedUser) || connectedUser.isAdmin()) {
                rideGroup.removeParticipant(user);
                rideRepository.save(ride);
            }

        }

        return "redirect:/rides/" + rideId;
    }

    private Page<Ride> getRidesFromRepository(SearchRideForm form) {
        SearchRideForm.SearchRideFormParser parser = form.parser();
        Pageable pageable = PageRequest.of(parser.getPage(), parser.getPageSize(), Sort.by("date").descending());
        return rideRepository.findByDateBetweenAndPublishedAtLessThan(
                parser.getFrom(),
                parser.getTo(),
                ZonedDateTime.now(),
                pageable);
    }


}
