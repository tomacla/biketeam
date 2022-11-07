package info.tomacla.biketeam.api;

import info.tomacla.biketeam.api.dto.RideDTO;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.web.ride.SearchRideForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams/{teamId}/rides")
public class RideAPI extends AbstractAPI {

    @Autowired
    private RideService rideService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<RideDTO>> getRides(@PathVariable String teamId,
                                                  @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                  @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                  @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                  @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

        final Team team = checkTeam(teamId);

        SearchRideForm form = SearchRideForm.builder()
                .withFrom(from)
                .withTo(to)
                .withPage(page)
                .withPageSize(pageSize)
                .get();

        final SearchRideForm.SearchRideFormParser parser = form.parser();

        Page<Ride> rides = rideService.searchRides(
                Set.of(team.getId()),
                parser.getPage(),
                parser.getPageSize(),
                parser.getFrom(),
                parser.getTo()
        );

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Pages", String.valueOf(rides.getTotalPages()));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(rides.getContent().stream().map(RideDTO::valueOf).collect(Collectors.toList()));

    }

    @GetMapping(path = "/{rideId}", produces = "application/json")
    public ResponseEntity<RideDTO> getRide(@PathVariable String teamId, @PathVariable String rideId) {

        checkTeam(teamId);

        return rideService.get(teamId, rideId)
                .map(value -> ResponseEntity.ok().body(RideDTO.valueOf(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
