package info.tomacla.biketeam.web.team;

import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.feed.FeedEntity;
import info.tomacla.biketeam.domain.feed.FeedOptions;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamConfiguration;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.service.UserRoleService;
import info.tomacla.biketeam.service.feed.FeedService;
import info.tomacla.biketeam.service.file.ThumbnailService;
import info.tomacla.biketeam.web.AbstractController;
import info.tomacla.biketeam.web.SearchFeedForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/{teamId}")
public class TeamController extends AbstractController {

    @Autowired
    private ThumbnailService thumbnailService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private FeedService feedService;

    @GetMapping(value = {"", "/"})
    public String getFeed(@PathVariable("teamId") String teamId,
                          @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                          @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                          @RequestParam(value = "onlyMyFeed", required = false, defaultValue = "false") boolean onlyMyFeed,
                          @ModelAttribute("error") String error,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        final TeamConfiguration teamConfiguration = team.getConfiguration();
        if (!teamConfiguration.isFeedVisible()) {
            return viewHandler.redirect(team, "/maps");
        }

        if (from == null) {
            from = feedService.getDefaultFrom(Set.of(teamId));
        }

        SearchFeedForm form = SearchFeedForm.builder()
                .withFrom(from)
                .withTo(to)
                .withOnlyMyFeed(onlyMyFeed)
                .get();

        final SearchFeedForm.SearchFeedFormParser parser = form.parser();

        FeedOptions options = new FeedOptions(
                parser.getFrom(),
                parser.getTo(),
                parser.isOnlyMyFeed()
        );


        List<FeedEntity> feed = feedService.listFeed(getUserFromPrincipal(principal).orElse(null), team, options);

        addGlobalValues(principal, model, team.getName(), team);
        model.addAttribute("feed", feed);
        model.addAttribute("formdata", form);
        model.addAttribute("users", team.getRoles().stream().map(UserRole::getUser).collect(Collectors.toSet()));
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_root";
    }

    @GetMapping(value = "/join")
    public RedirectView joinTeam(@PathVariable("teamId") String teamId,
                                 RedirectAttributes attributes,
                                 Principal principal,
                                 Model model) {

        final Team team = checkTeam(teamId);

        try {
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
            if (optionalConnectedUser.isPresent()) {
                User connectedUser = optionalConnectedUser.get();
                if (!team.isMember(connectedUser)) {
                    userRoleService.save(new UserRole(team, connectedUser, Role.MEMBER));
                }
            }
            return viewHandler.redirectView(team, "/");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/");
        }

    }

    @GetMapping(value = "/leave")
    public RedirectView leaveTeam(@PathVariable("teamId") String teamId,
                                  RedirectAttributes attributes,
                                  Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        try {
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

            if (optionalConnectedUser.isPresent()) {
                User connectedUser = optionalConnectedUser.get();
                if (team.isMember(connectedUser)) {
                    userRoleService.delete(team, connectedUser);
                }
            }

            return viewHandler.redirectView(team, "/");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/");
        }
    }

    @ResponseBody
    @RequestMapping(value = "/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getTeamImage(@PathVariable("teamId") String teamId) {
        final Optional<ImageDescriptor> image = teamService.getImage(teamId);
        if (image.isPresent()) {
            try {

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", image.get().getExtension().getMediaType());
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(teamId + image.get().getExtension().getExtension())
                        .build());

                return new ResponseEntity<>(
                        Files.readAllBytes(image.get().getPath()),
                        headers,
                        HttpStatus.OK
                );
            } catch (IOException e) {
                throw new ServerErrorException("Error while reading team image : " + teamId, e);
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find team image : " + teamId);

    }

}
