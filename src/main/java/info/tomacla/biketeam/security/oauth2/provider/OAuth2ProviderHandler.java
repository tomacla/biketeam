package info.tomacla.biketeam.security.oauth2.provider;

import info.tomacla.biketeam.domain.user.User;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

/**
 * Strategie de resolution d'un compte a partir d'une identite OAuth2.
 * <p>
 * Un handler par registration declaree dans application.properties. Le dispatcheur
 * {@link info.tomacla.biketeam.security.oauth2.Oauth2AuthUserService} les indexe par
 * {@link #registrationId()} : ajouter ou retirer un fournisseur ne consiste plus qu'a
 * ajouter ou retirer une implementation.
 */
public interface OAuth2ProviderHandler {

    /**
     * Identifiant de la registration OAuth2 (spring.security.oauth2.client.registration.&lt;id&gt;).
     */
    String registrationId();

    /**
     * Resout (et persiste) le compte correspondant a l'identite presentee par le fournisseur.
     */
    User resolve(DefaultOAuth2User oauthUser);

}
