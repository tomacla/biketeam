package info.tomacla.biketeam.security.oauth2.provider;

import info.tomacla.biketeam.common.amqp.Exchanges;
import info.tomacla.biketeam.common.amqp.RoutingKeys;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.oauth2.link.AccountLinkService;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.amqp.BrokerService;
import info.tomacla.biketeam.service.amqp.dto.UserProfileImageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Connexion et liaison Google.
 * <p>
 * L'adresse email n'est consideree comme verifiee que si Google l'affirme
 * ({@code email_verified}), et le repli "recherche par email" ne retient qu'un compte dont
 * l'adresse est deja verifiee en base : sans ces deux regles, quiconque controle une boite Google
 * correspondant a une adresse saisie a la main par un autre utilisateur recupererait son compte.
 */
@Service
public class GoogleProviderHandler implements OAuth2ProviderHandler {

    private static final Logger log = LoggerFactory.getLogger(GoogleProviderHandler.class);

    private final UserService userService;
    private final BrokerService brokerService;
    private final AccountLinkService accountLinkService;

    @Autowired
    public GoogleProviderHandler(UserService userService,
                                 BrokerService brokerService,
                                 AccountLinkService accountLinkService) {
        this.userService = userService;
        this.brokerService = brokerService;
        this.accountLinkService = accountLinkService;
    }

    @Override
    public String registrationId() {
        return "google";
    }

    @Override
    public User resolve(DefaultOAuth2User user) {

        final Map<String, Object> attributes = user.getAttributes();
        final String googleId = (String) attributes.get("sub");
        final String email = (String) attributes.get("email");
        final boolean emailProven = Boolean.TRUE.equals(attributes.get("email_verified"));
        final String profileImage = (String) attributes.get("picture");

        log.debug("Load user with google id {}", googleId);

        final Optional<User> identityOwner = userService.getByGoogleId(googleId);

        // liaison a la session courante si (et seulement si) une intention explicite a ete posee ;
        // leve un conflit si l'identite ou l'adresse appartient deja a un autre compte
        final Optional<User> linkTarget = accountLinkService.resolveLinkTarget(
                registrationId(), googleId, identityOwner, email, emailProven);

        User u;

        if (linkTarget.isPresent()) {

            log.debug("Linking google id {} to current user", googleId);

            u = linkTarget.get();
            u.setGoogleId(googleId);
            accountLinkService.applyProviderEmail(u, email, emailProven);

        } else {

            Optional<User> optionalUser = identityOwner;
            if (optionalUser.isEmpty() && emailProven && !Strings.isBlank(email)) {
                // repli par email : uniquement sur un compte dont l'adresse a deja ete prouvee.
                // Les adresses saisies a la main dans /users/me ne donnent aucun droit.
                optionalUser = userService.getByEmail(email).filter(User::isEmailVerified);
            }

            if (optionalUser.isEmpty()) {

                log.debug("Register new user with google id {}", googleId);

                u = new User();
                u.setFirstName((String) attributes.get("given_name"));
                u.setLastName((String) attributes.get("family_name"));
                u.setGoogleId(googleId);

            } else {
                u = optionalUser.get();
                u.setGoogleId(googleId);
            }

            accountLinkService.applyProviderEmail(u, email, emailProven);

        }

        u = userService.save(u);
        if (!Strings.isBlank(profileImage)) {
            brokerService.sendToBroker(Exchanges.TASK, RoutingKeys.TASK_DOWNLOAD_PROFILE_IMAGE,
                    UserProfileImageDTO.valueOf(u.getId(), profileImage + "?.jpg"));
        }

        return u;

    }

}
