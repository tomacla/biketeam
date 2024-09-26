package info.tomacla.biketeam.web;

import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.domain.feed.FeedEntity;
import info.tomacla.biketeam.domain.feed.FeedOptions;
import info.tomacla.biketeam.domain.map.MapSorterOption;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.map.WindDirection;
import info.tomacla.biketeam.domain.parameter.Parameter;
import info.tomacla.biketeam.domain.parameter.ParameterRepository;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.Visibility;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.security.Authorities;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.UserRoleService;
import info.tomacla.biketeam.service.feed.FeedService;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.web.team.NewTeamForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/")
public class RootController extends AbstractController {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private ParameterRepository parameterRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private FeedService feedService;

    @Autowired
    private MapService mapService;

    @GetMapping(value = {"", "/"})
    public String getRoot(@RequestParam(required = false, name = "error") String error,
                          @ModelAttribute(name = "error") String modelError,
                          @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                          @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                          @RequestParam(value = "onlyMyFeed", required = false, defaultValue = "false") boolean onlyMyFeed,
                          HttpSession session,
                          Principal principal,
                          Model model) {

        handleErrors(error, modelError, model);

        final Optional<User> userFromPrincipal = getUserFromPrincipal(principal);
        if (userFromPrincipal.isPresent()) {

            final User user = userFromPrincipal.get();

            final List<Team> teams = teamService.getUserTeams(user);
            Set<String> teamIds = teams.stream().map(Team::getId).collect(Collectors.toSet());

            if (from == null) {
                from = feedService.getDefaultFrom(teamIds);
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

            // TODO should be user time zone and not UTC
            final List<FeedEntity> feeds = feedService.listFeed(user, teamIds, ZoneOffset.UTC, options);

            addGlobalValues(principal, model, null, null, session);
            model.addAttribute("feed", feeds);
            model.addAttribute("formdata", form);

            return "root_auth";

        } else {
            addGlobalValues(principal, model, null, null);
            if (error != null) {
                model.addAttribute("errors", List.of(error));
            }
            model.addAttribute("teams", teamService.getLast4());
            model.addAttribute("teamCount", teamService.count());
            return "root";
        }

    }

    @Deprecated
    @GetMapping("/maps")
    public String getMaps(@RequestParam(value = "lowerDistance", required = false) Double lowerDistance,
                          @RequestParam(value = "upperDistance", required = false) Double upperDistance,
                          @RequestParam(value = "lowerPositiveElevation", required = false) Double lowerPositiveElevation,
                          @RequestParam(value = "upperPositiveElevation", required = false) Double upperPositiveElevation,
                          @RequestParam(value = "sort", required = false) MapSorterOption sort,
                          @RequestParam(value = "windDirection", required = false) WindDirection windDirection,
                          @RequestParam(value = "type", required = false) MapType type,
                          @RequestParam(value = "name", required = false) String name,
                          @RequestParam(value = "centerAddress", required = false) String centerAddress,
                          @RequestParam(value = "centerAddressLat", required = false) Double centerAddressLat,
                          @RequestParam(value = "centerAddressLng", required = false) Double centerAddressLng,
                          @RequestParam(value = "distanceToCenter", required = false) Integer distanceToCenter,
                          @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                          @RequestParam(value = "pageSize", defaultValue = "18", required = false) int pageSize,
                          @ModelAttribute("error") String error,
                          Principal principal,
                          Model model) {

        return "redirect:/catalog/maps";

    }

    @GetMapping("/teams")
    public String getTeams(@RequestParam(value = "name", defaultValue = "", required = false) String name,
                           @RequestParam(value = "page", defaultValue = "0", required = false) int page,
                           @RequestParam(value = "pageSize", defaultValue = "12", required = false) int pageSize,
                           Principal principal,
                           Model model) {

        Page<Team> teams = teamService.searchTeams(
                page,
                pageSize,
                name,
                List.of(Visibility.PUBLIC, Visibility.PRIVATE)
        );

        addGlobalValues(principal, model, "Groupes", null);
        model.addAttribute("teams", teams.getContent());
        model.addAttribute("matches", teams.getTotalElements());
        model.addAttribute("pages", teams.getTotalPages());
        model.addAttribute("name", name);
        model.addAttribute("page", page);
        model.addAttribute("pageSize", pageSize);

        return "teams";

    }

    @GetMapping(value = "new")
    public String newTeam(Principal principal, Model model) {
        addGlobalValues(principal, model, "Créer un groupe", null);
        model.addAttribute("formdata", NewTeamForm.builder().get());
        model.addAttribute("timezones", getAllAvailableTimeZones());
        return "new";
    }

    @PostMapping(value = "new")
    public String submitNewTeam(NewTeamForm form, Principal principal, Model model) {

        try {

            final User targetAdmin = getUserFromPrincipal(principal).orElseThrow(() -> new IllegalStateException("User not authenticated"));

            final NewTeamForm.NewTeamFormParser parser = form.parser();

            String targetId = parser.getId().toLowerCase();
            targetId = teamService.getPermalink(targetId);

            teamService.get(targetId).ifPresent(team -> {
                throw new IllegalArgumentException("Team " + team.getId() + " already exists");
            });

            final Team newTeam = new Team();
            newTeam.setId(targetId);
            newTeam.setName(parser.getName());
            newTeam.setCity(parser.getCity());
            newTeam.setCountry(parser.getCountry());
            newTeam.getDescription().setDescription(parser.getDescription());
            newTeam.getConfiguration().setTimezone(parser.getTimezone());

            teamService.save(newTeam, true);

            userRoleService.save(new UserRole(newTeam, targetAdmin, Role.ADMIN));

            addAuthorityToCurrentSession(Authorities.teamAdmin(newTeam.getId()));

            return "redirect:/" + newTeam.getId();

        } catch (Exception e) {
            addGlobalValues(principal, model, "Créer une team", null);
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            model.addAttribute("timezones", getAllAvailableTimeZones());
            return "new";
        }


    }


    @GetMapping(value = "login")
    public String loginPage(@RequestParam(value = "requestUri", required = false) final String referer, Principal principal, Model model) {
        addGlobalValues(principal, model, "Connexion", null);
        return "login";
    }

    @RequestMapping(value = "/legal-mentions", method = RequestMethod.GET)
    public String termsOfService(Principal principal, Model model) {

        Optional<Parameter> optionalLegalMentions = parameterRepository.findById("LEGAL_MENTIONS");

        model.addAttribute("content", optionalLegalMentions.isPresent() ? optionalLegalMentions.get().getValue() : "");

        addGlobalValues(principal, model, "Mentions légales", null);
        return "legal_mentions";
    }


    @ResponseBody
    @RequestMapping(value = "/misc/{imageName}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getMiscImage(@PathVariable("imageName") String imageName) {
        return getStaticImage(imageName);
    }

    private ResponseEntity<byte[]> getStaticImage(String image) {
        try {
            Path file = fileService.getFile(FileRepositories.MISC_IMAGES, image);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", MediaType.IMAGE_PNG_VALUE);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename(image)
                    .build());

            return new ResponseEntity<>(
                    Files.readAllBytes(file),
                    headers,
                    HttpStatus.OK
            );
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @ResponseBody
    @RequestMapping(value = "/robots.txt", method = RequestMethod.GET)
    public String robotsTxt() {

        String robotsTxt = """
                User-agent: *
                Disallow: /admin
                """;

        return robotsTxt;
    }

    private static void handleErrors(String error, String modelError, Model model) {
        List<String> errors = new ArrayList<>();
        if (!ObjectUtils.isEmpty(error)) {
            errors.add(error);
        }
        if (!ObjectUtils.isEmpty(modelError)) {
            errors.add(modelError);
        }
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
        }
    }

}
