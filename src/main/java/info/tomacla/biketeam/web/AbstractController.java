package info.tomacla.biketeam.web;


import info.tomacla.biketeam.common.Dates;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.UrlService;
import info.tomacla.biketeam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.ui.Model;

import java.security.Principal;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractController {

    @Autowired
    protected TeamService teamService;

    @Autowired
    protected UserService userService;

    @Autowired
    private UrlService urlService;

    // TODO do this automatically with annotation or other way
    protected void addGlobalValues(Principal principal, Model model, String pageTitle, Team team) {

        model.addAttribute("_pagetitle", pageTitle);
        model.addAttribute("_date_formatter", Dates.frenchFormatter);
        model.addAttribute("_time_formatter", Dates.timeFormatter);
        model.addAttribute("_authenticated", false);
        model.addAttribute("_admin", false);
        model.addAttribute("_team_admin", false);
        model.addAttribute("_team_member", false);
        model.addAttribute("_domains", teamService.findAllTeamWithDomain());
        model.addAttribute("_siteUrl", urlService.getSiteUrl());
        model.addAttribute("_embed", false);

        getUserFromPrincipal(principal).ifPresent(user -> {
            model.addAttribute("_authenticated", true);
            model.addAttribute("_admin", user.isAdmin());
            model.addAttribute("_profile_image", user.getProfileImage());
            model.addAttribute("_user_id", user.getId());
            model.addAttribute("_identity", user.getIdentity());
            if (team != null) {
                model.addAttribute("_team_admin", user.isAdmin(team.getId()));
                model.addAttribute("_team_member", user.isMember(team.getId()));
            }
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

    protected Optional<User> getUserFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken wrapperPrincipal = (OAuth2AuthenticationToken) principal;
            OAuth2UserDetails oauthprincipal = (OAuth2UserDetails) wrapperPrincipal.getPrincipal();
            return userService.get(oauthprincipal.getUsername());
        }
        if (principal instanceof RememberMeAuthenticationToken) {
            RememberMeAuthenticationToken wrapperPrincipal = (RememberMeAuthenticationToken) principal;
            OAuth2UserDetails oauthprincipal = (OAuth2UserDetails) wrapperPrincipal.getPrincipal();
            return userService.get(oauthprincipal.getUsername());
        }
        return Optional.empty();
    }

    protected boolean isAdmin(Principal principal, String teamId) {
        boolean admin = false;
        final Optional<User> optionalUser = getUserFromPrincipal(principal);
        if (optionalUser.isPresent()) {
            final User user = optionalUser.get();
            admin = user.isAdmin() || user.isAdmin(teamId);
        }
        return admin;

    }

    protected List<String> getAllAvailableTimeZones() {
        return ZoneId.getAvailableZoneIds().stream().map(ZoneId::of).map(ZoneId::toString).sorted().collect(Collectors.toList());
    }

    protected Team checkTeam(String teamId) {
        return teamService.get(teamId).orElseThrow(() -> new IllegalArgumentException("Unknown team " + teamId));
    }

    protected String redirectToRides(Team team) {
        return createRedirect(team, "/rides");
    }

    protected String redirectToTrips(Team team) {
        return createRedirect(team, "/trips");
    }

    protected String redirectToRide(Team team, String rideId) {
        return createRedirect(team, "/rides/" + rideId);
    }

    protected String redirectToTrip(Team team, String tripId) {
        return createRedirect(team, "/trips/" + tripId);
    }

    protected String redirectToMaps(Team team) {
        return createRedirect(team, "/maps");
    }

    protected String redirectToMap(Team team, String mapId) {
        return createRedirect(team, "/maps/" + mapId);
    }

    protected String redirectToFeed(Team team) {
        return createRedirect(team, "/");
    }

    protected String createRedirect(Team team, String suffix) {
        if (team.getConfiguration().isDomainConfigured()) {
            return "redirect:" + suffix;
        }
        return "redirect:/" + team.getId() + suffix;
    }

}