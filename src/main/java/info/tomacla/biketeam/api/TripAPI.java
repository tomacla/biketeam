package info.tomacla.biketeam.api;

import info.tomacla.biketeam.api.dto.TripDTO;
import info.tomacla.biketeam.domain.reaction.Reaction;
import info.tomacla.biketeam.domain.reaction.ReactionContent;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.ReactionService;
import info.tomacla.biketeam.service.TripService;
import info.tomacla.biketeam.web.trip.SearchTripForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams/{teamId}/trips")
public class TripAPI extends AbstractAPI {

    @Autowired
    private TripService tripService;

    @Autowired
    private ReactionService reactionService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<TripDTO>> getTrips(@PathVariable String teamId,
                                                  @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                  @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                  @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                  @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

        final Team team = checkTeam(teamId);

        SearchTripForm form = SearchTripForm.builder()
                .withFrom(from)
                .withTo(to)
                .withPage(page)
                .withPageSize(pageSize)
                .get();

        final SearchTripForm.SearchTripFormParser parser = form.parser();

        Page<Trip> trips = tripService.searchTrips(
                Set.of(team.getId()),
                parser.getPage(),
                parser.getPageSize(),
                parser.getFrom(),
                parser.getTo(),
                true
        );

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Pages", String.valueOf(trips.getTotalPages()));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(trips.getContent().stream().map(TripDTO::valueOf).collect(Collectors.toList()));

    }

    @GetMapping(path = "/{tripId}", produces = "application/json")
    public ResponseEntity<TripDTO> getTrip(@PathVariable String teamId, @PathVariable String tripId) {

        checkTeam(teamId);

        return tripService.get(teamId, tripId)
                .map(value -> ResponseEntity.ok().body(TripDTO.valueOf(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(path = "/{tripId}/reactions", consumes = "text/plain")
    public void addReaction(@PathVariable("teamId") String teamId,
                            @PathVariable("tripId") String tripId,
                            @RequestBody String content,
                            Principal principal) {

        final Team team = checkTeam(teamId);

        Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
        if (optionalTrip.isEmpty()) {
            return;
        }

        Trip trip = optionalTrip.get();
        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isEmpty()) {
            return;
        }

        User connectedUser = optionalConnectedUser.get();
        ReactionContent parsedContent = ReactionContent.valueOfUnicode(content);
        Reaction reaction = new Reaction();
        reaction.setTarget(trip);
        reaction.setContent(parsedContent.unicode());
        reaction.setUser(connectedUser);

        reactionService.save(reaction);

    }

    @DeleteMapping(value = "/{tripId}/reactions")
    public void removeReaction(@PathVariable("teamId") String teamId,
                               @PathVariable("tripId") String tripId,
                               Principal principal) {

        final Team team = checkTeam(teamId);


        Optional<Trip> optionalTrip = tripService.get(team.getId(), tripId);
        if (optionalTrip.isEmpty()) {
            return;
        }

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isPresent()) {

            User connectedUser = optionalConnectedUser.get();
            final Optional<Reaction> optionalReaction = reactionService.getReaction(tripId, connectedUser.getId());

            Reaction reaction = optionalReaction.get();
            if (optionalReaction.isPresent()) {
                reactionService.delete(reaction.getId());
            }

        }

    }

}
