package info.tomacla.biketeam.web;


import info.tomacla.biketeam.domain.global.GlobalData;
import info.tomacla.biketeam.domain.global.GlobalDataRepository;
import info.tomacla.biketeam.security.AdminAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.ui.Model;

import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public abstract class AbstractController {

    private static final DateTimeFormatter frenchFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.FRANCE);

    @Autowired
    protected GlobalDataRepository globalDataRepository;

    // FIXME do this automatically
    protected void addGlobalValues(Principal principal, Model model, String pageTitle) {

        @SuppressWarnings("OptionalGetWithoutIsPresent") GlobalData data = globalDataRepository.findById(1L).get();
        model.addAttribute("_sitename", data.getSitename());
        model.addAttribute("_description", data.getDescription());
        model.addAttribute("_pagetitle", pageTitle);
        model.addAttribute("global", data);
        model.addAttribute("_date_formatter", frenchFormatter);
        model.addAttribute("_authenticated", false);

        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken wrapperPrincipal = (OAuth2AuthenticationToken) principal;
            DefaultOAuth2User oauthprincipal = (DefaultOAuth2User) wrapperPrincipal.getPrincipal();
            model.addAttribute("_authenticated", true);
            model.addAttribute("_admin", AdminAuthority.check(wrapperPrincipal.getAuthorities()));
            model.addAttribute("_strava_id", oauthprincipal.getAttributes().get("id"));
            model.addAttribute("_identity", oauthprincipal.getAttributes().get("firstname") + " " + oauthprincipal.getAttributes().get("lastname"));
            model.addAttribute("_profile_image", oauthprincipal.getAttributes().get("profile_medium"));
        }

    }

}