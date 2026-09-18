package info.tomacla.biketeam.service.auth;

import org.altcha.altcha.v2.Altcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protection anti-robot des formulaires publics qui declenchent un envoi de mail (/register,
 * /forgot-password).
 * <p>
 * Trois barrieres independantes, toutes invisibles ou presque pour un humain :
 * <ul>
 *     <li>un champ piege (honeypot) masque en CSS, que seuls les robots remplissent ;</li>
 *     <li>un horodatage signe pose a l'affichage du formulaire : un envoi trop rapide trahit un
 *     script ;</li>
 *     <li>une preuve de travail ALTCHA, qui exige l'execution de JavaScript et rend chaque envoi
 *     couteux.</li>
 * </ul>
 * Motivation : des robots utilisaient l'inscription et le mot de passe oublie pour inonder de
 * mails des adresses tierces (email bombing), au detriment de la reputation SMTP de l'instance.
 * <p>
 * La cle HMAC est tiree au demarrage : un redemarrage invalide seulement les formulaires ouverts
 * a ce moment-la. Comme {@link RateLimitService}, l'etat est local a l'instance, ce qui suffit au
 * deploiement mono-instance actuel.
 */
@Service
public class BotProtectionService {

    private static final Logger log = LoggerFactory.getLogger(BotProtectionService.class);

    private static final String ALGORITHM = "PBKDF2/SHA-256";

    private static final int MAX_TRACKED_CHALLENGES = 10_000;

    /**
     * Nom du champ piege : un intitule plausible, que les robots remplissent volontiers.
     */
    public static final String HONEYPOT_FIELD = "website";

    /**
     * Issue du controle, dans l'ordre de priorite des verifications.
     */
    public enum Verdict {
        /** Toutes les barrieres sont franchies. */
        HUMAN,
        /** Le champ piege est rempli : l'appelant fait semblant d'accepter, sans rien envoyer. */
        HONEYPOT,
        /** Formulaire envoye trop vite apres son affichage. */
        TOO_FAST,
        /** Horodatage ou preuve de travail absent, invalide, expire ou deja utilise. */
        CHALLENGE_FAILED
    }

    private final String secret;

    private final Map<String, Long> usedChallenges = new ConcurrentHashMap<>();

    private Clock clock = Clock.systemUTC();

    @Value("${auth.bot-protection.min-fill-seconds:3}")
    private int minFillSeconds;

    @Value("${auth.bot-protection.altcha-cost:5000}")
    private int altchaCost;

    @Value("${auth.bot-protection.altcha-validity-minutes:15}")
    private int altchaValidityMinutes;

    public BotProtectionService() {
        final byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        this.secret = HexFormat.of().formatHex(key);
    }

    /**
     * Nouveau defi ALTCHA, au format JSON attendu par le widget.
     */
    public String createChallenge() {
        try {
            return Altcha.createChallenge(new Altcha.CreateChallengeOptions()
                    .algorithm(ALGORITHM)
                    .cost(altchaCost)
                    .expiresAt(clock.millis() / 1000 + Duration.ofMinutes(altchaValidityMinutes).toSeconds())
                    .hmacSignatureSecret(secret)).toJson();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create ALTCHA challenge", e);
        }
    }

    /**
     * Horodatage signe a placer dans le formulaire au moment de son affichage.
     */
    public String issueFormStamp() {
        final String timestamp = String.valueOf(clock.millis());
        return timestamp + "." + hmac(timestamp);
    }

    /**
     * Controle un envoi de formulaire. La preuve de travail n'est consommee que si les autres
     * barrieres sont franchies : elle ne peut ensuite plus etre rejouee.
     */
    public Verdict check(String altchaPayload, String honeypot, String formStamp) {

        if (honeypot != null && !honeypot.isEmpty()) {
            return Verdict.HONEYPOT;
        }

        final Long renderedAt = readFormStamp(formStamp);
        if (renderedAt == null) {
            return Verdict.CHALLENGE_FAILED;
        }
        if (clock.millis() - renderedAt < Duration.ofSeconds(minFillSeconds).toMillis()) {
            return Verdict.TOO_FAST;
        }

        return verifyAltcha(altchaPayload) ? Verdict.HUMAN : Verdict.CHALLENGE_FAILED;

    }

    /**
     * Message a afficher a un humain arrete par erreur. Le champ piege n'en a pas : il ne doit
     * jamais etre signale.
     */
    public static String errorMessage(Verdict verdict) {
        return switch (verdict) {
            case TOO_FAST -> "Formulaire envoyé trop rapidement. Patientez quelques secondes puis réessayez.";
            case CHALLENGE_FAILED -> "La vérification anti-robot a échoué. Réessayez.";
            default -> throw new IllegalArgumentException("No error message for " + verdict);
        };
    }

    private Long readFormStamp(String formStamp) {

        if (formStamp == null) {
            return null;
        }

        final int separator = formStamp.indexOf('.');
        if (separator <= 0) {
            return null;
        }

        final String timestamp = formStamp.substring(0, separator);
        final String signature = formStamp.substring(separator + 1);
        if (!MessageDigest.isEqual(hmac(timestamp).getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8))) {
            return null;
        }

        try {
            return Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            return null;
        }

    }

    private boolean verifyAltcha(String payload) {

        if (payload == null || payload.isBlank()) {
            return false;
        }

        final Altcha.Payload parsed;
        final Altcha.VerifySolutionResult result;
        try {
            parsed = Altcha.parsePayload(payload);
            result = Altcha.verifySolution(parsed.challenge(), parsed.solution(), secret, Altcha.kdf(ALGORITHM));
        } catch (Exception e) {
            log.debug("Malformed ALTCHA payload", e);
            return false;
        }

        // un defi sans expiration serait rejouable indefiniment : il n'a pas ete emis ici
        final Long expiresAt = parsed.challenge().parameters().expiresAt();
        if (!result.verified() || expiresAt == null) {
            return false;
        }

        // anti-rejeu : un defi resolu une fois sert a un seul envoi, jusqu'a son expiration
        final long now = clock.millis() / 1000;
        usedChallenges.values().removeIf(expiry -> expiry < now);
        if (usedChallenges.size() >= MAX_TRACKED_CHALLENGES) {
            // garde-fou memoire, atteint seulement sous une attaque massive : on refuse plutot que
            // d'oublier les defis deja utilises, qui redeviendraient rejouables
            log.warn("Too many ALTCHA challenges tracked, refusing submission");
            return false;
        }
        return usedChallenges.putIfAbsent(parsed.challenge().signature(), expiresAt) == null;

    }

    private String hmac(String value) {
        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign form stamp", e);
        }
    }

}
