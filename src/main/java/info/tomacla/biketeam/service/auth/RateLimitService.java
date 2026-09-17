package info.tomacla.biketeam.service.auth;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitation de debit en memoire, a fenetre glissante d'une heure.
 * <p>
 * Protege les routes non authentifiees qui declenchent un envoi de mail (/register,
 * /forgot-password, renvois de lien) : sans elle, l'instance servirait de relais de spam et
 * perdrait sa reputation SMTP.
 * <p>
 * Limites assumees : les compteurs ne sont pas partages entre instances et sont remis a zero
 * au redemarrage. C'est suffisant pour le deploiement mono-instance actuel (docker-compose).
 */
@Service
public class RateLimitService {

    private static final Duration WINDOW = Duration.ofHours(1);

    private static final int MAX_TRACKED_KEYS = 10_000;

    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    /**
     * Enregistre une tentative et indique si elle est autorisee.
     *
     * @return vrai si le quota n'est pas atteint, faux si l'appelant doit refuser l'operation
     */
    public boolean tryAcquire(String key, int maxPerHour) {

        if (key == null || maxPerHour <= 0) {
            return false;
        }

        if (attempts.size() > MAX_TRACKED_KEYS) {
            purge();
        }

        final Instant now = Instant.now();
        final Instant limit = now.minus(WINDOW);

        final Deque<Instant> timestamps = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(limit)) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxPerHour) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }

    }

    /**
     * Cle d'identification de l'appelant : adresse IP telle que vue par le conteneur.
     */
    public String clientKey(HttpServletRequest request, String prefix) {
        final String ip = request == null ? "unknown" : request.getRemoteAddr();
        return prefix + ":" + ip;
    }

    private void purge() {
        final Instant limit = Instant.now().minus(WINDOW);
        attempts.forEach((key, timestamps) -> {
            synchronized (timestamps) {
                while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(limit)) {
                    timestamps.pollFirst();
                }
                if (timestamps.isEmpty()) {
                    attempts.remove(key);
                }
            }
        });
    }

}
