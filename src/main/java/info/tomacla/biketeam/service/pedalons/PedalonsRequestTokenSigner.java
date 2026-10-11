package info.tomacla.biketeam.service.pedalons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Jeton de demande de migration (biketeam -> navigateur -> Pedalons).
 * <p>
 * JWS compact HS256 : base64url(en-tete).base64url(charge utile).base64url(signature), sans
 * remplissage, signe avec la cle decodee de PEDALONS_REQUEST_KEY. Pas de bibliotheque JWT : un
 * HmacSHA256 sur les octets ASCII de "en-tete64.charge64" suffit. L'en-tete et l'ordre des claims
 * sont fixes par le contrat ; le vecteur de test du plan est repris dans les tests.
 */
@Component
public class PedalonsRequestTokenSigner {

    public static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    public static final String ISSUER = "biketeam";
    public static final String AUDIENCE = "pedalons:biketeam-migration";
    public static final int VERSION = 1;
    public static final long VALIDITY_SECONDS = 3600;

    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

    // instance privee : la configuration globale de Jackson ne doit pas pouvoir changer les octets signes
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final PedalonsProperties properties;

    @Autowired
    public PedalonsRequestTokenSigner(PedalonsProperties properties) {
        this.properties = properties;
    }

    /**
     * Construit et signe le jeton d'une demande.
     */
    public String createToken(String requestId, Instant now, String teamId, String teamName, String requestedBy,
                              boolean dryRun, boolean reset, PedalonsSummary summary) {
        return sign(payload(requestId, now.getEpochSecond(), teamId, teamName, requestedBy, dryRun, reset,
                returnUrl(properties.getSiteUrl(), teamId), summary), properties.getRequestKey());
    }

    /**
     * Charge utile JSON, sans espace, claims dans l'ordre du contrat.
     */
    public String payload(String requestId, long iat, String teamId, String teamName, String requestedBy,
                          boolean dryRun, boolean reset, String returnUrl, PedalonsSummary summary) {

        Map<String, Object> summaryClaim = new LinkedHashMap<>();
        summaryClaim.put("places", summary.getPlaces());
        summaryClaim.put("routes", summary.getRoutes());
        summaryClaim.put("rides", summary.getRides());
        summaryClaim.put("rideTemplates", summary.getRideTemplates());
        summaryClaim.put("trips", summary.getTrips());
        summaryClaim.put("tripStages", summary.getTripStages());
        summaryClaim.put("publications", summary.getPublications());
        summaryClaim.put("faqPage", summary.isFaqPage());
        summaryClaim.put("logo", summary.isLogo());

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", ISSUER);
        claims.put("aud", AUDIENCE);
        claims.put("ver", VERSION);
        claims.put("jti", requestId);
        claims.put("iat", iat);
        claims.put("exp", iat + VALIDITY_SECONDS);
        claims.put("teamId", teamId);
        claims.put("teamName", teamName);
        claims.put("requestedBy", requestedBy);
        claims.put("dryRun", dryRun);
        claims.put("reset", reset);
        claims.put("returnUrl", returnUrl);
        claims.put("summary", summaryClaim);

        try {
            return objectMapper.writeValueAsString(claims);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize request token payload", e);
        }
    }

    /**
     * Signe une charge utile deja serialisee (octets UTF-8).
     */
    public static String sign(String payloadJson, byte[] key) {
        final String signingInput = B64.encodeToString(HEADER_JSON.getBytes(StandardCharsets.UTF_8))
                + "." + B64.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            final byte[] signature = mac.doFinal(signingInput.getBytes(StandardCharsets.US_ASCII));
            return signingInput + "." + B64.encodeToString(signature);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to sign request token", e);
        }
    }

    /**
     * returnUrl signe : exactement {site.url}/{teamId}/admin/pedalons/callback, que Pedalons
     * compare a la lettre (protection contre la redirection ouverte).
     */
    public static String returnUrl(String siteUrl, String teamId) {
        return siteUrl + "/" + teamId + "/admin/pedalons/callback";
    }

}
