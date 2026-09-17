package info.tomacla.biketeam.web;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.Authorities;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.security.passkey.PasskeySuggestionService;
import info.tomacla.biketeam.service.NotificationService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.url.UrlService;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.mockito.Mockito.mock;

/**
 * Outillage commun aux tests MockMvc autonomes des controleurs d'authentification.
 * <p>
 * Le montage est volontairement "standalone" : aucun contexte Spring n'est demarre, les
 * dependances sont injectees a la main. Les vues ne sont pas rendues, seul le nom de vue est
 * verifie - ce qui suffit pour tester la logique de decision des controleurs.
 */
public final class ControllerTestSupport {

    private ControllerTestSupport() {
    }

    /**
     * Renseigne les dependances communes a tous les controleurs (AbstractController) avec des
     * mocks neutres, puis construit un MockMvc autonome.
     */
    public static MockMvc mockMvc(AbstractController controller) {

        ReflectionTestUtils.setField(controller, "teamService", mock(TeamService.class));
        ReflectionTestUtils.setField(controller, "notificationService", mock(NotificationService.class));
        ReflectionTestUtils.setField(controller, "urlService", mock(UrlService.class));
        // mock neutre : suggestionNeeded() repond false, aucun bandeau passkey dans les vues
        ReflectionTestUtils.setField(controller, "passkeySuggestionService", mock(PasskeySuggestionService.class));
        ReflectionTestUtils.setField(controller, "siteName", "BikeTeam");

        return build(controller);

    }

    /**
     * Variante sans pre-remplissage, pour les tests qui veulent leurs propres mocks.
     */
    public static MockMvc build(Object controller) {

        // un prefixe distinct du chemin de la requete evite l'erreur "circular view path"
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".ftlh");

        return MockMvcBuilders.standaloneSetup(controller)
                .setViewResolvers(viewResolver)
                .build();

    }

    /**
     * Authentification pleine (form login ou OAuth2) portant le principal de l'application.
     */
    public static Authentication authentication(User user) {
        OAuth2UserDetails details = OAuth2UserDetails.create(user);
        return UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
    }

    /**
     * Authentification issue d'un cookie remember-me : insuffisante pour modifier un moyen de
     * connexion.
     */
    public static Authentication rememberMeAuthentication(User user) {
        OAuth2UserDetails details = OAuth2UserDetails.create(user);
        return new RememberMeAuthenticationToken("remember-me-key", details, details.getAuthorities());
    }

    /**
     * Principal etranger a l'application (jamais un OAuth2UserDetails).
     */
    public static Authentication foreignAuthentication() {
        return UsernamePasswordAuthenticationToken.authenticated("someone", null,
                java.util.List.of(Authorities.user()));
    }

}
