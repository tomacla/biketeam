package info.tomacla.biketeam.api;

import info.tomacla.biketeam.api.dto.PublicationDTO;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.PublicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams/{teamId}/publications")
public class PublicationApi extends AbstractAPI {

    @Autowired
    private PublicationService publicationService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<List<PublicationDTO>> getPublications(@PathVariable String teamId,
                                                                @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                                @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                                @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                                                                @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

        final Team team = checkTeam(teamId);

        Page<Publication> publications = publicationService.searchPublications(
                Set.of(team.getId()),
                page,
                pageSize,
                ZonedDateTime.of(from == null ? LocalDate.now().minus(1, ChronoUnit.MONTHS) : from, LocalTime.MIDNIGHT, ZoneOffset.UTC),
                ZonedDateTime.of(to == null ? LocalDate.now().plus(1, ChronoUnit.MONTHS) : to, LocalTime.MIDNIGHT, ZoneOffset.UTC)
        );

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-Pages", String.valueOf(publications.getTotalPages()));

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(publications.getContent().stream().map(PublicationDTO::valueOf).collect(Collectors.toList()));

    }

    @GetMapping(path = "/{publicationId}", produces = "application/json")
    public ResponseEntity<PublicationDTO> getPublication(@PathVariable String teamId, @PathVariable String publicationId) {

        checkTeam(teamId);

        return publicationService.get(teamId, publicationId)
                .map(value -> ResponseEntity.ok().body(PublicationDTO.valueOf(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
