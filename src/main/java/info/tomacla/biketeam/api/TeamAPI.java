package info.tomacla.biketeam.api;

import info.tomacla.biketeam.api.dto.*;
import info.tomacla.biketeam.domain.feed.FeedOptions;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.Visibility;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.service.PlaceService;
import info.tomacla.biketeam.service.feed.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams")
public class TeamAPI extends AbstractAPI {

    @Autowired
    private PlaceService placeService;

    @Autowired
    private FeedService feedService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<TeamDTO>> getTeams(@RequestParam(value = "name", required = false) String name,
                                                  @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                  @RequestParam(value = "pageSize", defaultValue = "12", required = false) int pageSize) {

        Page<Team> teams = teamService.searchTeams(
                page,
                pageSize,
                name,
                List.of(Visibility.PUBLIC, Visibility.PRIVATE),
                Sort.Order.asc("name").ignoreCase()
        );

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Page", String.valueOf(page));
        responseHeaders.set("X-PageSize", String.valueOf(pageSize));
        responseHeaders.set("X-Pages", String.valueOf(teams.getTotalPages()));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(teams.getContent().stream().map(team -> TeamDTO.valueOf(team, false)).collect(Collectors.toList()));

    }

    @GetMapping(path = "/{teamId}", produces = "application/json")
    public ResponseEntity<TeamDTO> getTeam(@PathVariable String teamId) {
        return teamService.get(teamId)
                .map(value -> ResponseEntity.ok().body(TeamDTO.valueOf(value, true)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/{teamId}/feed", produces = "application/json")
    public ResponseEntity<List<Object>> getTeamFeed(@PathVariable String teamId) {

        final Team team = checkTeam(teamId);

        return ResponseEntity.ok().body(feedService.listFeed(null, team, new FeedOptions()).stream().map(f -> {
            if (f instanceof Publication) {
                return PartialPublicationDTO.valueOf((Publication) f);
            } else if (f instanceof Ride) {
                return PartialRideDTO.valueOf((Ride) f);
            } else if (f instanceof Trip) {
                return PartialTripDTO.valueOf((Trip) f);
            }
            return null;
        }).filter(f -> f != null).collect(Collectors.toList()));

    }

    @GetMapping(path = "/{teamId}/members", produces = "application/json")
    public ResponseEntity<List<MemberDTO>> getTeamMembers(@PathVariable String teamId) {

        final Team team = checkTeam(teamId);

        return ResponseEntity.ok().body(team.getRoles().stream().map(ur -> MemberDTO.valueOf(ur.getUser())).collect(Collectors.toList()));

    }

    @GetMapping(path = "/{teamId}/faq", produces = "text/plain")
    public ResponseEntity<String> getFaq(@PathVariable String teamId) {

        final Team team = checkTeam(teamId);

        return ResponseEntity.ok().body(team.getConfiguration().getMarkdownPage());

    }

    @GetMapping(path = "/{teamId}/places", produces = "application/json")
    public ResponseEntity<List<PlaceDTO>> getTeamPlaces(@PathVariable String teamId) {

        final Team team = checkTeam(teamId);

        return ResponseEntity.ok().body(placeService.listPlaces(teamId).stream().map(p -> PlaceDTO.valueOf(p)).collect(Collectors.toList()));

    }

}
