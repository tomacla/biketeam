package info.tomacla.biketeam.security.password;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitation des tentatives de connexion, en fenetre glissante.
 * <p>
 * Le comptage est fait par couple (adresse email, ORIGINE de la requete), et non par adresse
 * seule : un verrou porte uniquement par l'email permettrait a n'importe qui connaissant l'adresse
 * d'un utilisateur (elles sont visibles des membres d'une equipe) de le maintenir indefiniment
 * hors de son compte avec quelques requetes erronees toutes les quinze minutes, alors meme qu'il
 * presente le bon mot de passe. Seule l'origine fautive est donc verrouillee.
 * <p>
 * Un verrou global sur l'adresse subsiste pour les attaques reellement distribuees : il ne se
 * declenche que lorsque {@value #DISTRIBUTED_ORIGINS_THRESHOLD} origines distinctes ont atteint le
 * seuil sur la meme adresse. Le cout d'un deni de service cible devient alors celui d'un botnet,
 * et le trafic correspondant est du vrai bourrage d'identifiants.
 * <p>
 * ATTENTION : les compteurs sont en memoire. Ils ne sont donc PAS partages entre instances et
 * sont remis a zero au redemarrage. C'est suffisant pour le deploiement mono-instance actuel
 * (docker-compose) ; un passage en multi-instances imposerait un stockage partage (base ou cache).
 */
@Service
public class LoginAttemptService {

    private static final String MSG_LOCKED = "Trop de tentatives de connexion. Réessayez dans quelques minutes.";

    /**
     * Nombre d'origines distinctes ayant atteint le seuil a partir duquel l'adresse est
     * verrouillee pour tout le monde.
     */
    public static final int DISTRIBUTED_ORIGINS_THRESHOLD = 3;

    /**
     * Origine utilisee lorsque l'appelant ne fournit pas d'adresse IP.
     */
    private static final String UNKNOWN_ORIGIN = "unknown";

    /**
     * Garde-fou memoire : au dela, les entrees les plus anciennes sont purgees.
     */
    private static final int MAX_TRACKED_EMAILS = 10_000;

    @Value("${auth.login.max-attempts:10}")
    private int maxAttempts;

    @Value("${auth.login.lock-minutes:15}")
    private int lockMinutes;

    /**
     * email normalise -> origine -> horodatages des echecs.
     */
    private final Map<String, Map<String, Deque<Instant>>> failures = new ConcurrentHashMap<>();

    public LoginAttemptService() {
        // constructeur utilise par Spring : les seuils sont injectes par @Value
    }

    /**
     * Constructeur destine aux tests unitaires (aucun contexte Spring, donc aucune @Value).
     */
    public LoginAttemptService(int maxAttempts, int lockMinutes) {
        this.maxAttempts = maxAttempts;
        this.lockMinutes = lockMinutes;
    }

    public void assertNotLocked(String email) {
        assertNotLocked(email, null);
    }

    public void assertNotLocked(String email, String ip) {
        if (isLocked(email, ip)) {
            throw new LockedException(MSG_LOCKED);
        }
    }

    public boolean isLocked(String email) {
        return isLocked(email, null);
    }

    public boolean isLocked(String email, String ip) {

        final String key = key(email);
        if (key == null) {
            return false;
        }

        final Map<String, Deque<Instant>> origins = failures.get(key);
        if (origins == null) {
            return false;
        }

        synchronized (origins) {

            purgeAll(origins);

            int lockedOrigins = 0;
            for (Deque<Instant> attempts : origins.values()) {
                if (attempts.size() >= maxAttempts) {
                    lockedOrigins++;
                }
            }

            if (lockedOrigins >= DISTRIBUTED_ORIGINS_THRESHOLD) {
                // attaque distribuee : le verrou devient global sur l'adresse
                return true;
            }

            final Deque<Instant> attempts = origins.get(origin(ip));
            return attempts != null && attempts.size() >= maxAttempts;

        }

    }

    public void recordFailure(String email) {
        recordFailure(email, null);
    }

    public void recordFailure(String email, String ip) {

        final String key = key(email);
        if (key == null) {
            return;
        }

        if (failures.size() >= MAX_TRACKED_EMAILS) {
            // NE JAMAIS vider la table entiere : un attaquant bloque sur une adresse n'aurait alors
            // qu'a intercaler des echecs sur MAX_TRACKED_EMAILS adresses jetables pour effacer tous
            // les verrous en cours et reprendre son bourrage. Seules les entrees perimees partent.
            purgeExpiredEmails();
        }

        final Map<String, Deque<Instant>> origins = failures.computeIfAbsent(key, k -> new ConcurrentHashMap<>());

        synchronized (origins) {
            purgeAll(origins);
            origins.computeIfAbsent(origin(ip), o -> new ArrayDeque<>()).addLast(Instant.now());
        }

    }

    public void recordSuccess(String email) {
        recordSuccess(email, null);
    }

    /**
     * Une authentification reussie efface les compteurs de l'adresse, toutes origines confondues :
     * le titulaire legitime ne doit jamais rester verrouille apres avoir prouve son identite.
     */
    public void recordSuccess(String email, String ip) {
        final String key = key(email);
        if (key != null) {
            failures.remove(key);
        }
    }

    /**
     * Garde-fou memoire : purge les adresses dont toutes les tentatives sont sorties de la fenetre
     * glissante. Les verrous encore actifs sont preserves.
     */
    private void purgeExpiredEmails() {
        failures.values().removeIf(origins -> {
            synchronized (origins) {
                purgeAll(origins);
                return origins.isEmpty();
            }
        });
    }

    private void purgeAll(Map<String, Deque<Instant>> origins) {
        final Instant limit = Instant.now().minus(Duration.ofMinutes(lockMinutes));
        origins.entrySet().removeIf(entry -> {
            final Deque<Instant> attempts = entry.getValue();
            while (!attempts.isEmpty() && attempts.peekFirst().isBefore(limit)) {
                attempts.removeFirst();
            }
            return attempts.isEmpty();
        });
    }

    private static String origin(String ip) {
        return (ip == null || ip.isBlank()) ? UNKNOWN_ORIGIN : ip.trim();
    }

    private String key(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

}
