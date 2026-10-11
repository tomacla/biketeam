package info.tomacla.biketeam.service.pedalons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

/**
 * Reglages de la migration vers Pedalons.
 * <p>
 * La fonction n'est active que si les cinq valeurs sont renseignees ; aucune : fonction eteinte
 * (bouton masque, /internal/pedalons/** en 404, poller inerte). Une partie seulement : echec du
 * demarrage, une moitie de configuration etant toujours une erreur - comme cote Pedalons.
 * <p>
 * Les proprietes se resolvent depuis l'environnement ou le .env (PEDALONS_URL,
 * PEDALONS_INTERNAL_URL, PEDALONS_REQUEST_KEY, PEDALONS_TRIGGER_SECRET, PEDALONS_EXPORT_SECRET).
 * Les secrets ne sont jamais journalises.
 * <p>
 * PEDALONS_REDIRECT_STATUS (pedalons.redirect-status) : code des redirections d'un groupe bascule,
 * 301 ou 302, 302 par defaut - un 301 est mis en cache sans fin par les navigateurs et l'annulation
 * de la bascule ne le rattrape pas ; on passe a 301 une fois les bascules stabilisees. Valide au
 * demarrage, que la fonction soit active ou non.
 */
@Component
public class PedalonsProperties {

    private static final Logger log = LoggerFactory.getLogger(PedalonsProperties.class);

    public static final int MIN_REQUEST_KEY_BYTES = 32;

    public static final int DEFAULT_REDIRECT_STATUS = 302;

    private final String url;
    private final String internalUrl;
    private final byte[] requestKey;
    private final String triggerSecret;
    private final String exportSecret;
    private final String siteUrl;
    private final boolean enabled;
    private final int redirectStatus;

    /**
     * Code de redirection par defaut (302) : commodite pour les tests.
     */
    public PedalonsProperties(String url, String internalUrl, String requestKey, String triggerSecret,
                              String exportSecret, String siteUrl) {
        this(url, internalUrl, requestKey, triggerSecret, exportSecret, siteUrl, null);
    }

    @Autowired
    public PedalonsProperties(@Value("${pedalons.url:}") String url,
                              @Value("${pedalons.internal-url:}") String internalUrl,
                              @Value("${pedalons.request-key:}") String requestKey,
                              @Value("${pedalons.trigger-secret:}") String triggerSecret,
                              @Value("${pedalons.export-secret:}") String exportSecret,
                              @Value("${site.url}") String siteUrl,
                              @Value("${pedalons.redirect-status:}") String redirectStatus) {

        this.redirectStatus = parseRedirectStatus(redirectStatus);

        this.url = stripTrailingSlash(trim(url));
        this.internalUrl = stripTrailingSlash(trim(internalUrl));
        this.triggerSecret = trim(triggerSecret);
        this.exportSecret = trim(exportSecret);
        this.siteUrl = stripTrailingSlash(trim(siteUrl));

        final String rawKey = trim(requestKey);

        List<String> missing = new ArrayList<>();
        if (this.url.isEmpty()) missing.add("PEDALONS_URL");
        if (this.internalUrl.isEmpty()) missing.add("PEDALONS_INTERNAL_URL");
        if (rawKey.isEmpty()) missing.add("PEDALONS_REQUEST_KEY");
        if (this.triggerSecret.isEmpty()) missing.add("PEDALONS_TRIGGER_SECRET");
        if (this.exportSecret.isEmpty()) missing.add("PEDALONS_EXPORT_SECRET");

        if (missing.size() == 5) {
            this.enabled = false;
            this.requestKey = new byte[0];
            log.info("Pedalons migration disabled (no PEDALONS_* configuration)");
            return;
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Pedalons migration is partially configured, missing: " + String.join(", ", missing));
        }

        try {
            this.requestKey = Base64.getDecoder().decode(rawKey);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("PEDALONS_REQUEST_KEY is not valid base64");
        }
        if (this.requestKey.length < MIN_REQUEST_KEY_BYTES) {
            throw new IllegalStateException("PEDALONS_REQUEST_KEY must decode to at least " + MIN_REQUEST_KEY_BYTES + " bytes");
        }

        requireHttps(this.internalUrl);

        this.enabled = true;
        log.info("Pedalons migration enabled, target {} (internal {})", this.url, this.internalUrl);
    }

    /**
     * Les appels M2M passent par Internet avec le secret en en-tete (pas de VPN) : https obligatoire,
     * http seulement vers la machine locale (developpement).
     */
    static void requireHttps(String url) {
        final URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("PEDALONS_INTERNAL_URL is not a valid URL");
        }
        final String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        final String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        final boolean local = host.equals("localhost") || host.equals("127.0.0.1") || host.equals("[::1]");
        if (!scheme.equals("https") && !(scheme.equals("http") && local)) {
            throw new IllegalStateException("PEDALONS_INTERNAL_URL must be https:// (http:// only towards localhost)");
        }
    }

    static int parseRedirectStatus(String value) {
        final String raw = trim(value);
        if (raw.isEmpty()) {
            return DEFAULT_REDIRECT_STATUS;
        }
        if (raw.equals("301") || raw.equals("302")) {
            return Integer.parseInt(raw);
        }
        throw new IllegalStateException("PEDALONS_REDIRECT_STATUS must be 301 or 302, got: " + raw);
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Code HTTP des redirections (GET/HEAD) d'un groupe bascule : 301 ou 302.
     */
    public int getRedirectStatus() {
        return redirectStatus;
    }

    /**
     * URL publique de Pedalons, sans / final : cible de la redirection du navigateur.
     */
    public String getUrl() {
        return url;
    }

    /**
     * URL de Pedalons pour les appels M2M (declenchement et statut), sans / final : l'URL publique
     * en https, en general la meme que getUrl().
     */
    public String getInternalUrl() {
        return internalUrl;
    }

    public byte[] getRequestKey() {
        return requestKey.clone();
    }

    public String getTriggerSecret() {
        return triggerSecret;
    }

    /**
     * site.url de biketeam sans / final : base du returnUrl signe.
     */
    public String getSiteUrl() {
        return siteUrl;
    }

    /**
     * Verification a temps constant du secret d'export presente par Pedalons. Le hachage prealable
     * evite de fuiter la longueur du secret attendu.
     */
    public boolean isValidExportSecret(String candidate) {
        if (!enabled || candidate == null || candidate.isEmpty()) {
            return false;
        }
        return constantTimeEquals(candidate, exportSecret);
    }

    public static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(sha256(a), sha256(b));
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String stripTrailingSlash(String value) {
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

}
