package info.tomacla.biketeam.security.password;

import org.springframework.security.core.AuthenticationException;

/**
 * Levee uniquement APRES validation du mot de passe : elle ne divulgue donc jamais
 * l'existence d'un compte a un tiers.
 */
public class EmailNotVerifiedException extends AuthenticationException {

    private final String userId;
    private final String email;

    public EmailNotVerifiedException(String userId, String email) {
        super("Votre adresse email n'est pas encore confirmée.");
        this.userId = userId;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

}
