package info.tomacla.biketeam.web;


import info.tomacla.biketeam.common.datatype.Dates;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.security.session.SecurityContextService;
import info.tomacla.biketeam.service.NotificationService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.url.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractController {

    @Autowired
    protected TeamService teamService;

    @Autowired
    protected UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SecurityContextService securityContextService;

    @Value("${site.name}")
    private String siteName;

    @Autowired
    private UrlService urlService;

    protected ViewHandler viewHandler = new ViewHandler();

    private static final AuthenticationTrustResolver TRUST_RESOLVER = new AuthenticationTrustResolverImpl();

    /**
     * Vrai si l'authentification courante ne repose que sur un cookie remember-me.
     * <p>
     * Les cookies ont une validite de 30 jours et sont poses systematiquement
     * (rememberMe.alwaysRemember) : un cookie vole suffit a naviguer sur le site. Toute operation
     * qui modifie un MOYEN DE CONNEXION (adresse email servant d'identifiant, premier mot de
     * passe) doit donc exiger une authentification fraiche, faute de quoi le vol du cookie
     * permettrait une prise de controle definitive du compte.
     */
    protected boolean isRememberMeAuthentication(Principal principal) {
        return principal instanceof Authentication authentication && TRUST_RESOLVER.isRememberMe(authentication);
    }

    protected void addGlobalValues(Principal principal, Model model, String pageTitle, Team team) {
        this.addGlobalValues(principal, model, pageTitle, team, null);
    }

    // TODO do this automatically with annotation or other way
    protected void addGlobalValues(Principal principal, Model model, String pageTitle, Team team, HttpSession session) {

        model.addAttribute("_pagetitle", pageTitle == null ? siteName : pageTitle);
        model.addAttribute("_sitename", siteName);
        model.addAttribute("_currentYear", Year.now().getValue());
        model.addAttribute("_date_formatter", Dates.frenchFormatter);
        model.addAttribute("_date_short_formatter", Dates.frenchShortFormatter);
        model.addAttribute("_time_formatter", Dates.timeFormatter);
        model.addAttribute("_date_today", LocalDate.now(getZoneId(team)));
        model.addAttribute("_authenticated", false);
        model.addAttribute("_admin", false);
        model.addAttribute("_team_admin", false);
        model.addAttribute("_team_member", false);
        model.addAttribute("_siteUrl", urlService.getSiteUrl());
        model.addAttribute("_embed", false);
        model.addAttribute("_fullSize", false);

        if (session != null && session.getId() != null) {
            model.addAttribute("_session", session.getId());
        }

        getUserFromPrincipal(principal).ifPresent(user -> {

            final List<Team> teams = teamService.getUserTeams(user);

            model.addAttribute("_authenticated", true);
            model.addAttribute("_user", user);

            model.addAttribute("_admin", user.isAdmin());
            model.addAttribute("_user_teams", teams);

            if (team != null) {
                model.addAttribute("_team_admin", team.isAdmin(user));
                model.addAttribute("_team_member", team.isMember(user));
            }

            model.addAttribute("_notifications", notificationService.listUnviewedByUser(user));

        });

        if (team != null) {
            model.addAttribute("team", team);
        }

    }

    protected void addOpenGraphValues(
            Team team,
            Model model,
            String title,
            String image,
            String url,
            String description) {

        Map<String, String> og = new HashMap<>();
        if (team.getDescription().getTwitter() != null) {
            og.put("twitter:image:src", image);
            og.put("twitter:site", "@" + team.getDescription().getTwitter());
            og.put("twitter:card", "summary_large_image");
            og.put("twitter:title", title);
            og.put("twitter:description", description);
        }

        og.put("og:image", image);
        og.put("og:image:alt", "Détails");
        og.put("og:image:width", "1200");
        og.put("og:image:height", "600");
        og.put("og:site_name", team.getName());
        og.put("og:type", "object");
        og.put("og:title", title);
        og.put("og:url", url);
        og.put("og:description", description);

        model.addAttribute("og", og);

    }

    /**
     * Tout type d'authentification est accepte (OAuth2, remember-me, form login) des lors que le
     * principal est un OAuth2UserDetails : c'est le seul principal pose par l'application.
     */
    protected Optional<User> getUserFromPrincipal(Principal principal) {
        if (principal instanceof Authentication a && a.getPrincipal() instanceof OAuth2UserDetails ud) {
            return userService.get(ud.getUsername());
        }
        return Optional.empty();
    }

    protected boolean isAdmin(Principal principal, Team team) {
        boolean admin = false;
        final Optional<User> optionalUser = getUserFromPrincipal(principal);
        if (optionalUser.isPresent()) {
            final User user = optionalUser.get();
            admin = user.isAdmin() || team.isAdmin(user);
        }
        return admin;

    }

    protected List<String> getAllAvailableTimeZones() {
        return ZoneId.getAvailableZoneIds().stream().map(ZoneId::of).map(ZoneId::toString).sorted().collect(Collectors.toList());
    }

    protected ZoneId getZoneId(Team team) {
        if (team == null) {
            return ZoneOffset.UTC;
        }
        return ZoneId.of(team.getConfiguration().getTimezone());
    }

    protected Team checkTeam(String teamId) {
        // FIXME redirect exception to root
        return teamService.get(teamId).orElseThrow(() -> new IllegalArgumentException("Unknown team " + teamId));
    }


    protected String createRedirect(Team team, String suffix) {
        return "redirect:/" + team.getId() + suffix;
    }

    /**
     * L'enregistrement explicite du contexte est indispensable : sans lui l'autorite ajoutee
     * serait perdue des la requete suivante (Spring Security 6).
     */
    protected void addAuthorityToCurrentSession(GrantedAuthority authority,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {
        securityContextService.addAuthority(authority, request, response);
    }

}
