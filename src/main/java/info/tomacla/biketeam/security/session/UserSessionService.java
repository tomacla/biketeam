package info.tomacla.biketeam.security.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * Suppression des sessions HTTP d'un utilisateur.
 * <p>
 * La rotation de {@code auth_token_seed} n'invalide QUE les cookies remember-me : la graine n'est
 * relue que pour recalculer la signature d'un cookie. Les sessions deja ouvertes sont, elles,
 * persistees (spring.session.store-type=jdbc) et rechargees telles quelles par
 * SecurityContextHolderFilter, sans jamais etre reconfrontees a la base. Sans cette classe, un
 * attaquant disposant d'une session active la conserverait apres la reinitialisation du mot de
 * passe de la victime, ce qui viderait de son sens le scenario "compte compromis".
 * <p>
 * L'index par nom de principal est alimente par JdbcIndexedSessionRepository a partir du
 * SecurityContext serialise : {@code Authentication.getName()} renvoie
 * {@code UserDetails.getUsername()}, c'est-a-dire l'IDENTIFIANT utilisateur (voir
 * {@link info.tomacla.biketeam.security.OAuth2UserDetails#getUsername()}). La cle a passer ici est
 * donc l'id de l'utilisateur.
 */
@Service
public class UserSessionService {

    private static final Logger log = LoggerFactory.getLogger(UserSessionService.class);

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Autowired
    public UserSessionService(ObjectProvider<FindByIndexNameSessionRepository<? extends Session>> sessionRepository) {
        // ObjectProvider : le depot indexe n'existe que si spring-session est actif. Son absence
        // ne doit jamais empecher le demarrage ni faire echouer une reinitialisation.
        this.sessionRepository = sessionRepository.getIfAvailable();
    }

    /**
     * Supprime toutes les sessions HTTP de l'utilisateur, y compris la session courante.
     *
     * @return le nombre de sessions supprimees
     */
    public int invalidateAll(String userId) {

        if (userId == null || sessionRepository == null) {
            if (sessionRepository == null) {
                log.warn("No indexed session repository available : HTTP sessions of user {} could not be invalidated", userId);
            }
            return 0;
        }

        try {

            final Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(userId);
            final Set<String> ids = sessions.keySet();
            ids.forEach(sessionRepository::deleteById);

            log.info("Invalidated {} HTTP session(s) of user {}", ids.size(), userId);
            return ids.size();

        } catch (Exception e) {
            log.error("Unable to invalidate HTTP sessions of user {}", userId, e);
            return 0;
        }

    }

}
