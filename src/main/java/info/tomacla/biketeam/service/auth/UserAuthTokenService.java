package info.tomacla.biketeam.service.auth;

import info.tomacla.biketeam.domain.user.UserAuthToken;
import info.tomacla.biketeam.domain.user.UserAuthTokenRepository;
import info.tomacla.biketeam.domain.user.UserAuthTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Gestion des tokens a usage unique (verification d'email, reinitialisation de mot de passe).
 * <p>
 * Le token clair n'est JAMAIS persiste : seule son empreinte SHA-256 (hexadecimal) est stockee.
 * SHA-256 et non BCrypt : la recherche doit rester deterministe et indexee, et les 256 bits
 * d'entropie du token rendent tout bruteforce hors-ligne sans objet.
 */
@Service
public class UserAuthTokenService {

    private static final Logger log = LoggerFactory.getLogger(UserAuthTokenService.class);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int TOKEN_BYTES = 32;

    @Autowired
    private UserAuthTokenRepository userAuthTokenRepository;

    @Value("${auth.email-verification.validity-hours:24}")
    private long emailVerificationValidityHours;

    @Value("${auth.password-reset.validity-hours:1}")
    private long passwordResetValidityHours;

    @Value("${auth.account-merge.validity-hours:1}")
    private long accountMergeValidityHours;

    public Duration getEmailVerificationValidity() {
        return Duration.ofHours(emailVerificationValidityHours);
    }

    public Duration getPasswordResetValidity() {
        return Duration.ofHours(passwordResetValidityHours);
    }

    /**
     * Validite courte : un lien de rattachement de compte donne acces a une fusion irreversible.
     */
    public Duration getAccountMergeValidity() {
        return Duration.ofHours(accountMergeValidityHours);
    }

    /**
     * Cree un token et renvoie sa valeur en clair (la seule fois ou elle existe).
     * Tout token actif du meme type pour cet utilisateur est invalide au prealable :
     * un seul lien vivant par type et par utilisateur.
     */
    @Transactional
    public String create(String userId, UserAuthTokenType type, String targetEmail, Duration ttl) {
        return create(userId, type, targetEmail, null, ttl);
    }

    /**
     * Variante a deux comptes, pour {@link UserAuthTokenType#ACCOUNT_MERGE} : {@code userId} est
     * le compte demandeur, {@code relatedUserId} le compte destinataire du lien.
     */
    @Transactional
    public String create(String userId, UserAuthTokenType type, String targetEmail,
                         String relatedUserId, Duration ttl) {

        invalidateAll(userId, type);

        final byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        final String clearToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        final Instant now = Instant.now();

        UserAuthToken token = new UserAuthToken();
        token.setUserId(userId);
        token.setType(type);
        token.setTokenHash(sha256Hex(clearToken));
        token.setTargetEmail(targetEmail == null ? null : targetEmail.trim().toLowerCase());
        token.setRelatedUserId(relatedUserId);
        token.setCreatedAt(now);
        token.setExpiresAt(now.plus(ttl));

        userAuthTokenRepository.save(token);

        return clearToken;

    }

    /**
     * Consomme un token : renvoie l'entite si et seulement si elle existe, est du bon type,
     * n'a pas deja ete consommee et n'est pas expiree. Le token est marque consomme.
     */
    @Transactional
    public Optional<UserAuthToken> consume(String clearToken, UserAuthTokenType type) {

        if (clearToken == null || clearToken.isBlank()) {
            return Optional.empty();
        }

        final String hash = sha256Hex(clearToken.trim());

        Optional<UserAuthToken> optionalToken = userAuthTokenRepository.findByTokenHash(hash);
        if (optionalToken.isEmpty()) {
            return Optional.empty();
        }

        UserAuthToken token = optionalToken.get();

        // aucune comparaison d'empreinte ici : la ligne a ete retrouvee PAR son empreinte, la
        // recomparer serait tautologique. Le secret lui-meme (256 bits) n'est jamais compare,
        // seul son hash circule, il n'y a donc pas de fuite par temps de reponse a couvrir.

        if (!type.equals(token.getType())) {
            return Optional.empty();
        }

        if (token.getConsumedAt() != null) {
            return Optional.empty();
        }

        if (token.getExpiresAt() == null || !token.getExpiresAt().isAfter(Instant.now())) {
            return Optional.empty();
        }

        token.setConsumedAt(Instant.now());
        userAuthTokenRepository.save(token);

        return Optional.of(token);

    }

    /**
     * Indique si un token est utilisable (existe, bon type, non consomme, non expire) sans le consommer.
     * Sert a refuser d'afficher un formulaire dont la soumission echouerait de toute facon.
     */
    public boolean isUsable(String clearToken, UserAuthTokenType type) {
        if (clearToken == null || clearToken.isBlank()) {
            return false;
        }
        return userAuthTokenRepository.findByTokenHash(sha256Hex(clearToken.trim()))
                .filter(token -> type.equals(token.getType()))
                .filter(token -> token.getConsumedAt() == null)
                .filter(token -> token.getExpiresAt() != null && token.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }

    /**
     * Invalide tous les tokens actifs d'un utilisateur pour un type donne.
     */
    @Transactional
    public void invalidateAll(String userId, UserAuthTokenType type) {
        final Instant now = Instant.now();
        List<UserAuthToken> tokens = userAuthTokenRepository.findByUserIdAndTypeAndConsumedAtIsNull(userId, type);
        tokens.forEach(token -> {
            token.setConsumedAt(now);
            userAuthTokenRepository.save(token);
        });
    }

    /**
     * Adresse visee par le lien encore vivant de ce type, s'il en existe un.
     * <p>
     * Le token clair n'etant pas conservable, "renvoyer le lien en cours" consiste en pratique
     * a en emettre un nouveau vers la meme adresse : cette methode fournit cette adresse.
     */
    public Optional<String> getPendingTargetEmail(String userId, UserAuthTokenType type) {
        final Instant now = Instant.now();
        return userAuthTokenRepository.findByUserIdAndTypeAndConsumedAtIsNull(userId, type).stream()
                .filter(token -> token.getExpiresAt() != null && token.getExpiresAt().isAfter(now))
                .filter(token -> token.getTargetEmail() != null)
                .max(Comparator.comparing(UserAuthToken::getCreatedAt))
                .map(UserAuthToken::getTargetEmail);
    }

    /**
     * Limitation de debit : vrai si l'utilisateur a deja demande maxPerHour tokens de ce type
     * dans la derniere heure.
     */
    public boolean isThrottled(String userId, UserAuthTokenType type, int maxPerHour) {
        final Instant after = Instant.now().minus(Duration.ofHours(1));
        return userAuthTokenRepository.countByUserIdAndTypeAndCreatedAtAfter(userId, type, after) >= maxPerHour;
    }

    /**
     * Limitation de debit cote DESTINATAIRE : sans elle, n'importe qui pourrait inonder de mails
     * de rattachement le proprietaire d'une adresse en revendiquant son adresse en boucle.
     */
    public boolean isRecipientThrottled(String relatedUserId, UserAuthTokenType type, int maxPerHour) {
        final Instant after = Instant.now().minus(Duration.ofHours(1));
        return userAuthTokenRepository
                .countByRelatedUserIdAndTypeAndCreatedAtAfter(relatedUserId, type, after) >= maxPerHour;
    }

    /**
     * Token de rattachement encore vivant, retrouve sans etre consomme.
     * Le controleur doit verifier la session AVANT de consommer : consommer d'abord brulerait
     * le lien a chaque ouverture depuis le mauvais navigateur (scanners de messagerie compris).
     */
    public Optional<UserAuthToken> peek(String clearToken, UserAuthTokenType type) {
        if (clearToken == null || clearToken.isBlank()) {
            return Optional.empty();
        }
        return userAuthTokenRepository.findByTokenHash(sha256Hex(clearToken.trim()))
                .filter(token -> type.equals(token.getType()))
                .filter(token -> token.getConsumedAt() == null)
                .filter(token -> token.getExpiresAt() != null && token.getExpiresAt().isAfter(Instant.now()));
    }

    @Transactional
    @Scheduled(cron = "0 15 4 * * *")
    public void purgeExpired() {
        try {
            userAuthTokenRepository.deleteByExpiresAtBefore(Instant.now());
        } catch (Exception e) {
            log.error("Unable to purge expired auth tokens", e);
        }
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

}
