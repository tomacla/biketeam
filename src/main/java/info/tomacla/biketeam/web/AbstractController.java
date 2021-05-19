package info.tomacla.biketeam.web;


import info.tomacla.biketeam.common.Dates;
import info.tomacla.biketeam.domain.global.*;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRepository;
import info.tomacla.biketeam.security.LocalDefaultOAuth2User;
import info.tomacla.biketeam.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.ui.Model;

import java.security.Principal;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractController {

    @Autowired
    protected ConfigurationService configurationService;

    @Autowired
    protected SiteDescriptionRepository siteDescriptionRepository;

    @Autowired
    protected SiteConfigurationRepository siteConfigurationRepository;

    @Autowired
    protected SiteIntegrationRepository siteIntegrationRepository;

    @Autowired
    protected UserRepository userRepository;

    // FIXME do this automatically
    protected void addGlobalValues(Principal principal, Model model, String pageTitle) {

        SiteDescription desc = configurationService.getSiteDescription();
        SiteConfiguration config = configurationService.getSiteConfiguration();
        SiteIntegration integ = configurationService.getSiteIntegration();

        model.addAttribute("_sitename", desc.getSitename());
        model.addAttribute("_description", desc.getDescription());
        model.addAttribute("_pagetitle", pageTitle);
        model.addAttribute("_desc", desc);
        model.addAttribute("_config", config);
        model.addAttribute("_integ", integ);
        model.addAttribute("_date_formatter", Dates.frenchFormatter);
        model.addAttribute("_authenticated", false);

        getUserFromPrincipal(principal).ifPresent(user -> {
            model.addAttribute("_authenticated", true);
            model.addAttribute("_admin", user.isAdmin());
            model.addAttribute("_strava_id", user.getStravaId());
            model.addAttribute("_identity", user.getIdentity());
            model.addAttribute("_profile_image", user.getProfileImage());
            model.addAttribute("_user_id", user.getId());
        });

    }

    protected Optional<User> getUserFromPrincipal(Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken wrapperPrincipal = (OAuth2AuthenticationToken) principal;
            LocalDefaultOAuth2User oauthprincipal = (LocalDefaultOAuth2User) wrapperPrincipal.getPrincipal();
            return userRepository.findById(oauthprincipal.getLocalUserId());
        }
        return Optional.empty();
    }

    protected List<String> getAllAvailableTimeZones() {
        return ZoneId.getAvailableZoneIds().stream().map(ZoneId::of).map(ZoneId::toString).sorted().collect(Collectors.toList());
    }


}