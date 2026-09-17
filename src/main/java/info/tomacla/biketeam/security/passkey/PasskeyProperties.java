package info.tomacla.biketeam.security.passkey;

import com.webauthn4j.data.client.Origin;
import info.tomacla.biketeam.service.url.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parametres du "relying party" WebAuthn.
 * <p>
 * Le RP ID est le domaine auquel la passkey est cryptographiquement liee : l'authentificateur
 * refusera de la presenter a tout autre domaine. Il est donc derive de {@code site.url}, et non
 * du domaine de la requete : une team disposant d'un domaine personnalise
 * ({@code TeamConfiguration.domain}) ne pourrait pas utiliser les passkeys du site principal.
 * Ce n'est pas une limitation nouvelle - les pages /login, /register et /users/me sont deja
 * rendues sur {@code _siteUrl} par la macro {@code common.teamUrl} - mais c'est la raison pour
 * laquelle il ne faut surtout pas calculer le RP ID a partir de {@code request.getServerName()}.
 */
@Component
public class PasskeyProperties {

    private static final Logger log = LoggerFactory.getLogger(PasskeyProperties.class);

    @Autowired
    private UrlService urlService;

    @Value("${site.name}")
    private String siteName;

    /**
     * Force le RP ID. A n'utiliser que pour un deploiement multi-sous-domaines ou les passkeys
     * doivent valoir pour tout le domaine parent. Doit rester un suffixe enregistrable de
     * l'origine, sinon le navigateur rejette silencieusement l'enregistrement.
     */
    @Value("${auth.passkey.rp-id:}")
    private String configuredRpId;

    /**
     * Origines supplementaires acceptees, separees par des virgules. Utile en developpement
     * (http://localhost:8080) ou derriere un reverse proxy expose sur plusieurs noms.
     */
    @Value("${auth.passkey.origins:}")
    private String configuredOrigins;

    @Value("${auth.passkey.challenge-validity-seconds:300}")
    private int challengeValiditySeconds;

    @Value("${auth.passkey.max-per-user:20}")
    private int maxPerUser;

    /**
     * Nombre maximum de demandes d'options d'authentification par heure et par adresse IP.
     * Les options sont servies sans authentification : sans quota, l'endpoint serait un
     * generateur gratuit de sessions HTTP.
     */
    @Value("${auth.passkey.login-options.max-per-hour:60}")
    private int loginOptionsMaxPerHour;

    private String rpId;
    private Set<Origin> origins;

    @PostConstruct
    public void init() {

        final String siteOrigin = normalizeOrigin(urlService.getSiteUrl());

        this.rpId = (configuredRpId == null || configuredRpId.isBlank())
                ? urlService.getCookieDomain()
                : configuredRpId.trim();

        final Set<Origin> resolved = new LinkedHashSet<>();
        resolved.add(Origin.create(siteOrigin));
        if (configuredOrigins != null && !configuredOrigins.isBlank()) {
            resolved.addAll(Arrays.stream(configuredOrigins.split(","))
                    .map(String::trim)
                    .filter(o -> !o.isEmpty())
                    .map(PasskeyProperties::normalizeOrigin)
                    .map(Origin::create)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
        this.origins = Set.copyOf(resolved);

        if (!siteOrigin.startsWith("https://") && !"localhost".equals(rpId)) {
            // le navigateur refuse l'API WebAuthn hors contexte securise : autant le dire au
            // demarrage plutot que de laisser le bouton echouer silencieusement en production
            log.warn("Passkeys : {} n'est pas en HTTPS, l'API WebAuthn sera indisponible dans le navigateur.", siteOrigin);
        }

    }

    public String getRpId() {
        return rpId;
    }

    public String getRpName() {
        return siteName;
    }

    public Set<Origin> getOrigins() {
        return origins;
    }

    public Duration getChallengeValidity() {
        return Duration.ofSeconds(challengeValiditySeconds);
    }

    public int getMaxPerUser() {
        return maxPerUser;
    }

    public int getLoginOptionsMaxPerHour() {
        return loginOptionsMaxPerHour;
    }

    /**
     * Origin n'accepte qu'un schema, un hote et un port : tout chemin ou slash final ferait
     * echouer la comparaison avec l'origine annoncee par le navigateur.
     */
    private static String normalizeOrigin(String url) {
        String tmp = url.trim();
        while (tmp.endsWith("/")) {
            tmp = tmp.substring(0, tmp.length() - 1);
        }
        return tmp;
    }

}
