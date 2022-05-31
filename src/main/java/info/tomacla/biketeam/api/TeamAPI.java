package info.tomacla.biketeam.api;

import info.tomacla.biketeam.api.dto.FeedDTO;
import info.tomacla.biketeam.api.dto.MemberDTO;
import info.tomacla.biketeam.api.dto.TeamDTO;
import info.tomacla.biketeam.common.data.Country;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.Visibility;
import info.tomacla.biketeam.web.SearchTeamForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams")
public class TeamAPI extends AbstractAPI {

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<TeamDTO>> getTeams(@RequestParam(value = "name", required = false) String name,
                                                  @RequestParam(value = "country", required = false) Country country,
                                                  @RequestParam(value = "city", required = false) String city,
                                                  @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                  @RequestParam(value = "pageSize", defaultValue = "12", required = false) int pageSize) {

        SearchTeamForm form = SearchTeamForm.builder()
                .withName(name)
                .withCity(city)
                .withCountry(country)
                .withPage(page)
                .withPageSize(pageSize)
                .get();

        final SearchTeamForm.SearchTeamFormParser parser = form.parser();

        Page<Team> teams = teamService.searchTeams(
                parser.getPage(),
                parser.getPageSize(),
                parser.getName(),
                parser.getCity(),
                parser.getCountry(),
                Sort.Order.desc("createdAt").ignoreCase()
        );

        HttpHeaders responseHeaders = new HttpHeaders();
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
    public ResponseEntity<List<FeedDTO>> getTeamFeed(@PathVariable String teamId) {

        final Team team = checkTeam(teamId);

        return ResponseEntity.ok().body(teamService.listFeed(team).stream().map(FeedDTO::valueOf).collect(Collectors.toList()));

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

}
