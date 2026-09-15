package info.tomacla.biketeam.security.oauth2;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.OAuth2UserDetails;
import info.tomacla.biketeam.security.oauth2.provider.OAuth2ProviderHandler;
import info.tomacla.biketeam.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dispatcheur : chaque fournisseur OAuth2 est traite par son propre
 * {@link OAuth2ProviderHandler}, indexe par registrationId.
 * <p>
 * Aucune logique specifique a un fournisseur ici : retirer la connexion Strava consistera a
 * supprimer {@code StravaProviderHandler} et sa registration, sans toucher a cette classe.
 */
@Service
public class Oauth2AuthUserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(Oauth2AuthUserService.class);

    private final UserService userService;
    private final Map<String, OAuth2ProviderHandler> handlers;

    @Autowired
    public Oauth2AuthUserService(UserService userService, List<OAuth2ProviderHandler> handlers) {
        this.userService = userService;
        this.handlers = handlers.stream().collect(Collectors.toMap(OAuth2ProviderHandler::registrationId, h -> h));
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2User user = (DefaultOAuth2User) super.loadUser(userRequest);
        final String registrationId = userRequest.getClientRegistration().getRegistrationId();
        return loadUser(user, registrationId);
    }

    protected OAuth2User loadUser(DefaultOAuth2User user, String registrationId) {

        final OAuth2ProviderHandler handler = handlers.get(registrationId);
        if (handler == null) {
            log.warn("No handler for OAuth2 registration {}", registrationId);
            return user;
        }

        final User u = handler.resolve(user);

        // authentification interactive : la graine de signature remember-me heritee de la
        // migration (= id utilisateur) est remplacee par une valeur aleatoire.
        // L'instance est mutee sur place, le principal construit ci-dessous porte donc la
        // graine a jour.
        userService.ensureAuthTokenSeed(u);

        return OAuth2UserDetails.create(u);

    }

}
