package info.tomacla.biketeam.web;

import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.web.forms.SearchRideForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Controller
@RequestMapping(value = "/rides")
public class RideController extends AbstractController {

    @Autowired
    private RideRepository rideRepository;

    @GetMapping
    public String getRides(Principal principal, Model model) {

        LocalDate to = LocalDate.now();
        LocalDate from = to.minus(1L, ChronoUnit.MONTHS);

        SearchRideForm form = new SearchRideForm();
        form.setFrom(from.format(DateTimeFormatter.ISO_LOCAL_DATE));
        form.setTo(to.format(DateTimeFormatter.ISO_LOCAL_DATE));

        addGlobalValues(principal, model, "Rides");
        model.addAttribute("rides", rideRepository.findByDateBetweenOrderByDateDesc(from, to));
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

    @PostMapping
    public String getRides(Principal principal, Model model,
                           SearchRideForm form) {

        LocalDate to = LocalDate.parse(form.getTo());
        LocalDate from = LocalDate.parse(form.getFrom());

        addGlobalValues(principal, model, "Rides");
        model.addAttribute("rides", rideRepository.findByDateBetweenOrderByDateDesc(from, to));
        model.addAttribute("formdata", form);
        return "rides";
    }

    @GetMapping(value = "/{rideId}")
    public String getRide(@PathVariable("rideId") String rideId,
                          Principal principal, Model model) {
        Ride ride = rideRepository.findById(rideId).get();
        addGlobalValues(principal, model, "Ride " + ride.getTitle());
        model.addAttribute("ride", ride);
        return "ride";
    }

}
