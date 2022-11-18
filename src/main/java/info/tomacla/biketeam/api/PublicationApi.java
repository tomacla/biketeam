package info.tomacla.biketeam.api;

import info.tomacla.biketeam.api.dto.PublicationDTO;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.reaction.Reaction;
import info.tomacla.biketeam.domain.reaction.ReactionContent;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams/{teamId}/publications")
public class PublicationApi extends AbstractAPI {

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private ReactionService reactionService;

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

    @PostMapping(path = "/{publicationId}/reactions", consumes = "text/plain")
    public void addReaction(@PathVariable("teamId") String teamId,
                            @PathVariable("publicationId") String publicationId,
                            @RequestBody String content,
                            Principal principal) {

        final Team team = checkTeam(teamId);

        Optional<Publication> optionalPublication = publicationService.get(team.getId(), publicationId);
        if (optionalPublication.isEmpty()) {
            return;
        }

        Publication publication = optionalPublication.get();
        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isEmpty()) {
            return;
        }

        User connectedUser = optionalConnectedUser.get();
        ReactionContent parsedContent = ReactionContent.valueOfUnicode(content);
        Reaction reaction = new Reaction();
        reaction.setTarget(publication);
        reaction.setContent(parsedContent.unicode());
        reaction.setUser(connectedUser);

        reactionService.save(team, publication, reaction);

    }

    @DeleteMapping(value = "/{publicationId}/reactions")
    public void removeReaction(@PathVariable("teamId") String teamId,
                               @PathVariable("publicationId") String publicationId,
                               Principal principal) {

        final Team team = checkTeam(teamId);

        Optional<Publication> optionalPublication = publicationService.get(team.getId(), publicationId);
        if (optionalPublication.isEmpty()) {
            return;
        }

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isPresent()) {

            User connectedUser = optionalConnectedUser.get();
            final Optional<Reaction> optionalReaction = reactionService.getReaction(publicationId, connectedUser.getId());

            Reaction reaction = optionalReaction.get();
            if (optionalReaction.isPresent()) {
                reactionService.delete(reaction.getId());
            }

        }

    }

}
