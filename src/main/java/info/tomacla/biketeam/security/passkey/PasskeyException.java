package info.tomacla.biketeam.security.passkey;

/**
 * Echec fonctionnel d'une operation passkey. Le message est destine a l'utilisateur : il ne doit
 * jamais contenir de detail technique exploitable (identifiant de credential, trace, etc.).
 */
public class PasskeyException extends RuntimeException {

    public PasskeyException(String message) {
        super(message);
    }

    public PasskeyException(String message, Throwable cause) {
        super(message, cause);
    }

}
