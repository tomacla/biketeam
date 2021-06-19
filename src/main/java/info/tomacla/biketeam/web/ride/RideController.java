package info.tomacla.biketeam.web.ride;

import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.Role;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/rides")
public class RideController extends AbstractController {

    @Autowired
    private RideService rideService;

    @GetMapping(value = "/{rideId}")
    public String getRide(@PathVariable("teamId") String teamId,
                          @PathVariable("rideId") String rideId,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        Optional<Ride> optionalRide = rideService.get(team.getId(), rideId);
        if (optionalRide.isEmpty()) {
            return redirectToRides(team.getId());
        }

        Ride ride = optionalRide.get();
        addGlobalValues(principal, model, "Ride " + ride.getTitle(), team);
        model.addAttribute("ride", ride);
        return "ride";
    }

    @GetMapping
    public String getRides(@PathVariable("teamId") String teamId,
                           @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                           @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                           @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                           @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
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
        return "rides";

    }

    @GetMapping(value = "/{rideId}/add-participant/{groupId}/{userId}")
    public String addParticipantToRide(@PathVariable("teamId") String teamId,
                                       @PathVariable("rideId") String rideId,
                                       @PathVariable("groupId") String groupId,
                                       @PathVariable("userId") String userId,
                                       Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        Optional<Ride> optionalRide = rideService.get(team.getId(), rideId);
        if (optionalRide.isEmpty()) {
            return redirectToRides(team.getId());
        }

        Ride ride = optionalRide.get();
        Optional<RideGroup> optionalGroup = ride.getGroups().stream().filter(rg -> rg.getId().equals(groupId)).findFirst();
        Optional<User> optionalUser = userService.get(userId);
        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isPresent() && optionalGroup.isPresent() && optionalUser.isPresent()) {
            RideGroup rideGroup = optionalGroup.get();
            User user = optionalUser.get();
            User connectedUser = optionalConnectedUser.get();

            if (user.equals(connectedUser) || connectedUser.isAdmin()) {

                if (!team.isAdminOrMember(user.getId())) {
                    team.addRole(user, Role.MEMBER);
                    teamService.save(team);
                }

                rideGroup.addParticipant(user);
                rideService.save(ride);
            }

        }

        return redirectToRide(team.getId(), rideId);
    }

    @GetMapping(value = "/{rideId}/remove-participant/{groupId}/{userId}")
    public String removeParticipantToRide(@PathVariable("teamId") String teamId,
                                          @PathVariable("rideId") String rideId,
                                          @PathVariable("groupId") String groupId,
                                          @PathVariable("userId") String userId,
                                          Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        Optional<Ride> optionalRide = rideService.get(team.getId(), rideId);
        if (optionalRide.isEmpty()) {
            return redirectToRides(team.getId());
        }

        Ride ride = optionalRide.get();
        Optional<RideGroup> optionalGroup = ride.getGroups().stream().filter(rg -> rg.getId().equals(groupId)).findFirst();
        Optional<User> optionalUser = userService.get(userId);
        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isPresent() && optionalGroup.isPresent() && optionalUser.isPresent()) {
            RideGroup rideGroup = optionalGroup.get();
            User user = optionalUser.get();
            User connectedUser = optionalConnectedUser.get();

            if (user.equals(connectedUser) || connectedUser.isAdmin()) {
                rideGroup.removeParticipant(user);
                rideService.save(ride);
            }

        }

        return redirectToRide(team.getId(), rideId);
    }


}
