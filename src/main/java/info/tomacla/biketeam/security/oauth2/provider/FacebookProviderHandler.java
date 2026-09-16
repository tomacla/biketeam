package info.tomacla.biketeam.security.oauth2.provider;

import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.security.oauth2.link.AccountLinkService;
import info.tomacla.biketeam.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Connexion et liaison Facebook.
 * <p>
 * Facebook ne renvoie l'adresse email que si la permission a ete accordee ; lorsqu'elle est
 * presente, elle est consideree comme verifiee par le fournisseur. Le repli "recherche par email"
 * ne retient neanmoins qu'un compte dont l'adresse est deja verifiee en base.
 */
@Service
public class FacebookProviderHandler implements OAuth2ProviderHandler {

    private static final Logger log = LoggerFactory.getLogger(FacebookProviderHandler.class);

    private final UserService userService;
    private final AccountLinkService accountLinkService;

    @Autowired
    public FacebookProviderHandler(UserService userService, AccountLinkService accountLinkService) {
        this.userService = userService;
        this.accountLinkService = accountLinkService;
    }

    @Override
    public String registrationId() {
        return "facebook";
    }

    @Override
    public User resolve(DefaultOAuth2User user) {

        final Map<String, Object> attributes = user.getAttributes();
        final String facebookId = (String) attributes.get("id");
        final String email = (String) attributes.get("email");
        // l'adresse n'est transmise que si l'utilisateur a accorde la permission : sa presence
        // vaut preuve cote fournisseur
        final boolean emailProven = !Strings.isBlank(email);

        log.debug("Load user with facebook id {}", facebookId);

        final Optional<User> identityOwner = userService.getByFacebookId(facebookId);

        final Optional<User> linkTarget = accountLinkService.resolveLinkTarget(
                registrationId(), facebookId, identityOwner, email, emailProven);

        User u;

        if (linkTarget.isPresent()) {

            log.debug("Linking facebook id {} to current user", facebookId);

            u = linkTarget.get();
            u.setFacebookId(facebookId);
            accountLinkService.applyProviderEmail(u, email, emailProven);

        } else {

            Optional<User> optionalUser = identityOwner;
            if (optionalUser.isEmpty() && emailProven) {
                // repli par email : uniquement sur un compte dont l'adresse a deja ete prouvee
                optionalUser = userService.getByEmail(email).filter(User::isEmailVerified);
            }

            if (optionalUser.isEmpty()) {

                log.debug("Register new user with facebook id {}", facebookId);

                final String fullName = (String) attributes.get("name");
                final String[] nameParts = fullName.split(" ");
                String firstName = fullName;
                String lastName = "";
                if (nameParts.length > 1) {
                    firstName = nameParts[0];
                    lastName = nameParts[1];
                }

                u = new User();
                u.setFirstName(firstName);
                u.setLastName(lastName);
                u.setFacebookId(facebookId);

            } else {
                u = optionalUser.get();
                u.setFacebookId(facebookId);
            }

            accountLinkService.applyProviderEmail(u, email, emailProven);

        }

        return userService.save(u);

    }

}
