package info.tomacla.biketeam.web;


import info.tomacla.biketeam.domain.global.GlobalData;
import info.tomacla.biketeam.domain.global.GlobalDataRepository;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRepository;
import info.tomacla.biketeam.security.LocalDefaultOAuth2User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.ui.Model;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;

public abstract class AbstractController {

    private static final DateTimeFormatter frenchFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.FRANCE);

    @Autowired
    protected GlobalDataRepository globalDataRepository;

    @Autowired
    protected UserRepository userRepository;

    // FIXME do this automatically
    protected void addGlobalValues(Principal principal, Model model, String pageTitle) {

        @SuppressWarnings("OptionalGetWithoutIsPresent") GlobalData data = globalDataRepository.findById(1L).get();
        model.addAttribute("_sitename", data.getSitename());
        model.addAttribute("_description", data.getDescription());
        model.addAttribute("_pagetitle", pageTitle);
        model.addAttribute("global", data);
        model.addAttribute("_date_formatter", frenchFormatter);
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


}