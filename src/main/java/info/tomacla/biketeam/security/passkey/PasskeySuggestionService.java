package info.tomacla.biketeam.security.passkey;

import info.tomacla.biketeam.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Faut-il proposer a l'utilisateur courant d'enregistrer une passkey ?
 * <p>
 * La question est posee au rendu de CHAQUE page : la reponse est donc mise en cache dans la
 * session HTTP, faute de quoi on ferait un COUNT par page et par utilisateur connecte. Le cache
 * est invalide par {@link PasskeyController} des qu'une passkey est ajoutee ou supprimee.
 * <p>
 * Une session qui enregistre une passkey depuis un AUTRE navigateur garde un cache perime et
 * continue de voir le bandeau jusqu'a sa prochaine connexion. C'est assume : contrairement a la
 * completion de compte, aucune redirection n'en depend, le seul degat est une suggestion de trop.
 */
@Service
public class PasskeySuggestionService {

    public static final String SESSION_ATTR = "BIKETEAM_HAS_PASSKEY";

    @Autowired
    private PasskeyService passkeyService;

    /**
     * Le bandeau n'est propose qu'aux comptes sans aucune passkey : une seule suffit a faire
     * disparaitre la sollicitation, les suivantes s'ajoutent depuis /users/me.
     */
    public boolean suggestionNeeded(User user) {
        return user != null && !hasPasskey(user);
    }

    private boolean hasPasskey(User user) {

        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            // hors requete (rendu d'un mail, tache planifiee) : rien a suggerer, personne ne lit
            return true;
        }

        final Object cached = attributes.getAttribute(SESSION_ATTR, RequestAttributes.SCOPE_SESSION);
        if (cached instanceof Boolean cachedValue) {
            return cachedValue;
        }

        final boolean hasPasskey = passkeyService.countByUser(user.getId()) > 0;
        attributes.setAttribute(SESSION_ATTR, hasPasskey, RequestAttributes.SCOPE_SESSION);

        return hasPasskey;

    }

    public void invalidate() {
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.removeAttribute(SESSION_ATTR, RequestAttributes.SCOPE_SESSION);
        }
    }

}
