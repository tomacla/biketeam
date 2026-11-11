package info.tomacla.biketeam.service.pedalons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.pedalons.PedalonsMigration;
import info.tomacla.biketeam.domain.pedalons.PedalonsMigrationRepository;
import info.tomacla.biketeam.domain.pedalons.PedalonsMigrationStatus;
import info.tomacla.biketeam.domain.pedalons.PedalonsRedirect;
import info.tomacla.biketeam.domain.pedalons.PedalonsRedirectRepository;
import info.tomacla.biketeam.domain.team.PedalonsState;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.pedalons.export.TeamSnapshotBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Migration d'une equipe biketeam vers Pedalons : demande, retour de Pedalons, declenchement,
 * suivi, bascule et annulation de la bascule.
 * <p>
 * Toute migration est incrementale : elle copie (ou met a jour) le groupe sur Pedalons et ne change
 * rien ici - le groupe reste modifiable, rien n'est redirige, et elle peut etre relancee autant de
 * fois que voulu. La bascule est manuelle et separee : l'administrateur du groupe lit le bilan puis
 * bascule ({@link #switchTeam}), ce qui enregistre la table d'URL de la derniere migration reussie
 * et redirige des lors toutes les URL du groupe. Ce qui est modifie ici apres cette migration
 * n'est pas sur Pedalons : a lui de relancer une migration juste avant de basculer.
 * <p>
 * Les transactions sont courtes et explicites (TransactionTemplate) : aucun appel HTTP vers
 * Pedalons ne se fait dans une transaction, et l'etat TRIGGERING est valide AVANT le
 * declenchement - l'export n'est servi que pour une migration active, que le worker Pedalons
 * peut lire des la reponse 202.
 */
@Service
public class PedalonsMigrationService {

    private static final Logger log = LoggerFactory.getLogger(PedalonsMigrationService.class);

    public static final String OUTCOME_CANCELLED = "cancelled";

    /** Duree de validite du jeton signe de demande (exp - iat). */
    public static final Duration REQUEST_VALIDITY = Duration.ofSeconds(PedalonsRequestTokenSigner.VALIDITY_SECONDS);
    /**
     * Duree de vie d'un grant cote Pedalons, comptee depuis la confirmation : pedalons.biketeam.grant-ttl
     * (defaut PT10M). A garder aligne si Pedalons la change.
     */
    public static final Duration PEDALONS_GRANT_TTL = Duration.ofMinutes(10);
    /**
     * Delai accorde au retour de Pedalons au-dela de l'exp du jeton : Pedalons accepte encore une
     * confirmation jusqu'a exp + 60 s (tolerance d'horloge), et le grant qu'il emet alors vit
     * PEDALONS_GRANT_TTL ; la marge restante couvre un ecart d'horloge et un navigateur lent. Tant que
     * ce delai court, la demande reste REQUESTED et son callback est accepte : le grant fait foi.
     */
    public static final Duration CALLBACK_GRACE = Duration.ofMinutes(15);
    /** Une demande REQUESTED n'aboutit plus au-dela (callback refuse, demande expiree par le poller). */
    public static final Duration REQUEST_ACCEPTANCE = REQUEST_VALIDITY.plus(CALLBACK_GRACE);
    /** Attentes avant chaque essai de declenchement (reseau ou 5xx) ; le rejeu est idempotent. */
    static final long[] TRIGGER_DELAYS_MS = {0, 2000, 5000};
    /** Duree maximale d'un declenchement : tous les essais, chacun au bout de ses delais reseau. */
    static final Duration MAX_TRIGGER_DURATION = Duration.ofMillis(java.util.Arrays.stream(TRIGGER_DELAYS_MS).sum())
            .plus(PedalonsClient.CONNECT_TIMEOUT.plus(PedalonsClient.READ_TIMEOUT).multipliedBy(TRIGGER_DELAYS_MS.length));
    /**
     * Au-dela, un TRIGGERING est considere comme interrompu (arret brutal) et rejoue avec le grant
     * garde en base. Borne basse : plus long qu'un declenchement complet ({@link #MAX_TRIGGER_DURATION},
     * pour ne pas doubler un declenchement encore en vol). Borne haute : le rejeu doit arriver avant
     * l'expiration du grant ({@link #PEDALONS_GRANT_TTL} depuis la confirmation, qui precede
     * triggeredAt), compte tenu du pas du poller ({@link PedalonsMigrationPoller#INTERVAL_MS}).
     */
    public static final Duration TRIGGERING_STUCK_AFTER = Duration.ofMinutes(3);
    /**
     * Delai d'execution d'un job : compte depuis le premier statut RUNNING rapporte par Pedalons
     * (runningSince), pas depuis le declenchement - Pedalons n'execute qu'un job a la fois, l'attente
     * en QUEUED derriere une autre equipe n'est pas de l'execution. Au-dela, sans etat terminal : TIMEOUT.
     */
    public static final Duration RUNNING_TIMEOUT = Duration.ofHours(6);
    /**
     * Attente maximale d'un job en QUEUED (ou dont Pedalons n'a encore rapporte aucun statut),
     * comptee depuis le declenchement, ou depuis le premier RUNNING s'il est revenu en QUEUED pour
     * une nouvelle tentative. Largement plus longue que RUNNING_TIMEOUT : plusieurs grosses
     * equipes peuvent passer avant. Tant qu'aucun delai n'est depasse, le groupe reste gele et
     * l'export servi.
     */
    public static final Duration QUEUED_TIMEOUT = Duration.ofHours(48);

    /**
     * Code du corps JSON d'un 404 de Pedalons pour un job inconnu : seul ce 404 conclut JOB_LOST.
     */
    public static final String JOB_NOT_FOUND = "BIKETEAM_JOB_NOT_FOUND";
    // lecture seule du bilan garde en base (result_json)
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern GRANT_PATTERN = Pattern.compile("^bmg_[A-Za-z0-9_-]{20,100}$");

    /** Permet aux tests de ne pas attendre. */
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    private final PedalonsProperties properties;
    private final PedalonsMigrationRepository migrationRepository;
    private final PedalonsRedirectRepository redirectRepository;
    private final TeamRepository teamRepository;
    private final PedalonsRequestTokenSigner signer;
    private final PedalonsClient client;
    private final TeamSnapshotBuilder snapshotBuilder;
    private final PedalonsRedirectCache redirectCache;
    private final TransactionTemplate tx;

    private Clock clock = Clock.systemUTC();
    private Sleeper sleeper = Thread::sleep;

    @Autowired
    public PedalonsMigrationService(PedalonsProperties properties,
                                    PedalonsMigrationRepository migrationRepository,
                                    PedalonsRedirectRepository redirectRepository,
                                    TeamRepository teamRepository,
                                    PedalonsRequestTokenSigner signer,
                                    PedalonsClient client,
                                    TeamSnapshotBuilder snapshotBuilder,
                                    PedalonsRedirectCache redirectCache,
                                    PlatformTransactionManager transactionManager) {
        this.properties = properties;
        this.migrationRepository = migrationRepository;
        this.redirectRepository = redirectRepository;
        this.teamRepository = teamRepository;
        this.signer = signer;
        this.client = client;
        this.snapshotBuilder = snapshotBuilder;
        this.redirectCache = redirectCache;
        this.tx = new TransactionTemplate(transactionManager);
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    void setSleeper(Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    /**
     * Pedalons ne derive un slug que d'un identifiant qui respecte ce motif (voir
     * BiketeamRequestTokenVerifier cote Pedalons) : les autres groupes ne sont pas migrables.
     */
    private static final java.util.regex.Pattern MIGRATABLE_TEAM_ID = java.util.regex.Pattern.compile("^[a-z0-9][a-z0-9_.-]{0,254}$");

    public boolean isMigratable(Team team) {
        return MIGRATABLE_TEAM_ID.matcher(team.getId()).matches();
    }

    public PedalonsSummary summary(Team team) {
        return snapshotBuilder.summary(team);
    }

    public List<PedalonsMigration> history(String teamId) {
        return migrationRepository.findByTeamIdOrderByCreatedAtDesc(teamId);
    }

    // ------------------------------------------------------------------------------------------
    // Demande
    // ------------------------------------------------------------------------------------------

    /**
     * Cree une demande et rend l'URL Pedalons vers laquelle rediriger le navigateur.
     */
    public String start(Team team, User user) {

        if (!properties.isEnabled()) {
            throw new PedalonsMigrationException("La migration vers Pédalons n'est pas activée.");
        }
        if (team.getPedalonsState() == PedalonsState.MIGRATED) {
            throw new PedalonsMigrationException("Ce groupe a déjà été migré vers Pédalons.");
        }
        if (!isMigratable(team)) {
            throw new PedalonsMigrationException("L'identifiant de ce groupe n'est pas migrable vers Pédalons.");
        }

        final Instant now = clock.instant();
        final String teamId = team.getId();

        final PedalonsMigration migration = tx.execute(status -> {
            if (migrationRepository.existsByTeamIdAndStatusIn(teamId, PedalonsMigrationStatus.ACTIVE)) {
                throw new PedalonsMigrationException("Une migration de ce groupe est déjà en cours.");
            }
            // une seule demande en attente par equipe : les precedentes n'aboutiront plus
            for (PedalonsMigration previous : migrationRepository.findByTeamIdAndStatusIn(teamId, List.of(PedalonsMigrationStatus.REQUESTED))) {
                previous.setStatus(PedalonsMigrationStatus.CANCELLED, now);
                migrationRepository.save(previous);
            }
            return migrationRepository.save(new PedalonsMigration(
                    UUID.randomUUID().toString(), teamId, user == null ? null : user.getId(), now));
        });

        final String token = signer.createToken(migration.getId(), now, teamId, team.getName(), displayName(user),
                snapshotBuilder.summary(team));

        log.info("Pedalons migration {} requested for team {}", migration.getId(), teamId);

        return properties.getUrl() + "/migration-biketeam?request=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    static String displayName(User user) {
        if (user == null) {
            return "Administrateur";
        }
        final String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return name.isEmpty() ? "Administrateur" : name;
    }

    // ------------------------------------------------------------------------------------------
    // Retour de Pedalons et declenchement
    // ------------------------------------------------------------------------------------------

    /**
     * Retour du navigateur depuis Pedalons, sur /{teamId}/admin/pedalons/callback. Rend un message
     * d'information pour la page d'administration, ou leve une PedalonsMigrationException.
     * <p>
     * Le requestId sert de "state" : il doit designer une demande en attente de CE groupe, faite
     * par CE compte biketeam (un lien de retour forge ou rejoue par un tiers est refuse).
     */
    public String callback(Team team, User user, String requestId, String grant, String outcome) {

        if (!properties.isEnabled()) {
            throw new PedalonsMigrationException("La migration vers Pédalons n'est pas activée.");
        }

        final Instant now = clock.instant();
        final String teamId = team.getId();

        final PedalonsMigration migration = pendingRequest(teamId, requestId, now)
                .orElseThrow(() -> new PedalonsMigrationException("Demande de migration inconnue, expirée ou déjà traitée."));

        if (migration.getRequestedBy() != null && (user == null || !migration.getRequestedBy().equals(user.getId()))) {
            throw new PedalonsMigrationException("Cette demande de migration a été faite depuis un autre compte.");
        }

        if (OUTCOME_CANCELLED.equals(outcome)) {
            tx.executeWithoutResult(status -> migrationRepository.transition(requestId,
                    PedalonsMigrationStatus.REQUESTED, PedalonsMigrationStatus.CANCELLED, now));
            log.info("Pedalons migration {} cancelled on Pedalons", requestId);
            return "La migration a été annulée.";
        }

        if (grant == null || !GRANT_PATTERN.matcher(grant).matches()) {
            throw new PedalonsMigrationException("Réponse de Pédalons invalide : autorisation absente ou mal formée.");
        }

        if (team.getPedalonsState() == PedalonsState.MIGRATED) {
            throw new PedalonsMigrationException("Ce groupe a déjà été migré vers Pédalons.");
        }

        final boolean moved = Boolean.TRUE.equals(tx.execute(status -> {
            final Optional<PedalonsMigration> fresh = pendingRequest(teamId, requestId, now);
            if (fresh.isEmpty()) {
                return false;
            }
            if (migrationRepository.existsByTeamIdAndStatusIn(teamId, PedalonsMigrationStatus.ACTIVE)) {
                throw new PedalonsMigrationException("Une migration de ce groupe est déjà en cours.");
            }
            final PedalonsMigration m = fresh.get();
            m.setStatus(PedalonsMigrationStatus.TRIGGERING, now);
            m.setGrantValue(grant);
            m.setTriggeredAt(now);
            migrationRepository.save(m);
            return true;
        }));
        if (!moved) {
            throw new PedalonsMigrationException("Demande de migration inconnue, expirée ou déjà traitée.");
        }
        final PedalonsMigrationStatus result = trigger(requestId);
        if (result == PedalonsMigrationStatus.RUNNING) {
            return "Migration lancée sur Pédalons.";
        }
        if (result == PedalonsMigrationStatus.CANCELLED) {
            throw new PedalonsMigrationException("La migration a été annulée par un administrateur de la plateforme.");
        }
        throw new PedalonsMigrationException("Pédalons a refusé la migration : "
                + migrationRepository.findById(requestId).map(PedalonsMigration::getErrorCode).orElse("erreur inconnue"));
    }

    private Optional<PedalonsMigration> pendingRequest(String teamId, String requestId, Instant now) {
        if (requestId == null) {
            return Optional.empty();
        }
        return migrationRepository.findByIdAndTeamId(requestId, teamId)
                .filter(m -> m.getStatus() == PedalonsMigrationStatus.REQUESTED)
                .filter(m -> acceptsCallback(m, now));
    }

    /**
     * Une demande REQUESTED accepte son retour de Pedalons jusqu'a exp du jeton + {@link #CALLBACK_GRACE}
     * (exclu) ; ensuite le poller l'expire.
     */
    static boolean acceptsCallback(PedalonsMigration migration, Instant now) {
        return now.isBefore(migration.getCreatedAt().plus(REQUEST_ACCEPTANCE));
    }

    /**
     * Declenchement M2M d'une migration TRIGGERING : 3 essais (0, 2, 5 s) sur erreur reseau ou
     * 5xx, avec le meme grant - Pedalons repond 200 avec le meme jobId a un rejeu.
     */
    PedalonsMigrationStatus trigger(String migrationId) {

        final PedalonsMigration migration = migrationRepository.findById(migrationId)
                .filter(m -> m.getStatus() == PedalonsMigrationStatus.TRIGGERING)
                .orElse(null);
        if (migration == null) {
            return null;
        }
        if (migration.getGrantValue() == null) {
            failTriggering(migrationId, "GRANT_LOST", "Grant no longer available", null);
            return PedalonsMigrationStatus.FAILED;
        }

        PedalonsClient.Response response = null;
        for (long delay : TRIGGER_DELAYS_MS) {
            if (delay > 0) {
                try {
                    sleeper.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            response = client.trigger(migration.getId(), migration.getGrantValue(), migration.getTeamId());
            if (!response.isRetryable()) {
                break;
            }
        }

        if (response != null && (response.status() == 200 || response.status() == 202)) {
            final String jobId = response.text("jobId");
            if (Strings.isBlank(jobId)) {
                failTriggering(migrationId, "INVALID_RESPONSE", "No jobId in Pedalons response", null);
                return PedalonsMigrationStatus.FAILED;
            }
            final Instant now = clock.instant();
            // conditionnel : une annulation (unswitch) pendant l'appel l'emporte ; le job cree
            // echouera alors sur l'export, qui n'est plus servi
            final Integer moved = tx.execute(status -> migrationRepository.markRunning(migrationId, jobId, now));
            if (moved == null || moved == 0) {
                final PedalonsMigrationStatus current = migrationRepository.findById(migrationId)
                        .map(PedalonsMigration::getStatus).orElse(null);
                log.warn("Pedalons migration {} triggered (job {}) but no longer TRIGGERING ({}): ignored", migrationId, jobId, current);
                return current;
            }
            log.info("Pedalons migration {} triggered, job {}", migrationId, jobId);
            return PedalonsMigrationStatus.RUNNING;
        }

        if (response == null || response.isRetryable()) {
            failTriggering(migrationId, "TRIGGER_UNAVAILABLE", "Pedalons could not be reached", null);
        } else {
            final String code = Optional.ofNullable(response.code()).orElse("HTTP_" + response.status());
            failTriggering(migrationId, code, response.text("message"), response.rawBody());
        }
        return migrationRepository.findById(migrationId).map(PedalonsMigration::getStatus).orElse(PedalonsMigrationStatus.FAILED);
    }

    /**
     * Echec d'un declenchement : seulement si la migration est encore TRIGGERING. Un autre
     * declenchement du meme grant (rejeu du poller, double clic) a pu la faire passer RUNNING
     * entre-temps : l'echec de celui-ci ne doit alors pas la terminer.
     */
    private void failTriggering(String migrationId, String code, String message, String rawBody) {
        fail(migrationId, code, message, rawBody, PedalonsMigrationStatus.TRIGGERING);
    }

    // ------------------------------------------------------------------------------------------
    // Suivi (PedalonsMigrationPoller)
    // ------------------------------------------------------------------------------------------

    public void pollAll() {
        if (!properties.isEnabled()) {
            return;
        }
        final Instant now = clock.instant();

        for (PedalonsMigration migration : migrationRepository.findByStatus(PedalonsMigrationStatus.RUNNING)) {
            try {
                poll(migration.getId());
            } catch (Exception e) {
                log.error("Unable to poll Pedalons migration {}", migration.getId(), e);
            }
        }

        // arret brutal entre le callback et la reponse de Pedalons : rejouer le declenchement
        for (PedalonsMigration migration : migrationRepository.findByStatus(PedalonsMigrationStatus.TRIGGERING)) {
            if (migration.getTriggeredAt() == null || migration.getTriggeredAt().isBefore(now.minus(TRIGGERING_STUCK_AFTER))) {
                try {
                    log.warn("Pedalons migration {} stuck in TRIGGERING, replaying", migration.getId());
                    trigger(migration.getId());
                } catch (Exception e) {
                    log.error("Unable to replay Pedalons migration {}", migration.getId(), e);
                }
            }
        }

        for (PedalonsMigration migration : migrationRepository.findByStatus(PedalonsMigrationStatus.REQUESTED)) {
            if (!acceptsCallback(migration, now)) {
                tx.executeWithoutResult(status -> migrationRepository.transition(migration.getId(),
                        PedalonsMigrationStatus.REQUESTED, PedalonsMigrationStatus.EXPIRED, clock.instant()));
            }
        }
    }

    void poll(String migrationId) {

        final PedalonsMigration migration = migrationRepository.findById(migrationId)
                .filter(m -> m.getStatus() == PedalonsMigrationStatus.RUNNING)
                .orElse(null);
        if (migration == null) {
            return;
        }

        final Instant now = clock.instant();

        final PedalonsClient.Response response = client.status(migration.getPedalonsJobId());

        if (response.status() == 404 && JOB_NOT_FOUND.equals(response.code())) {
            // seul un 404 signe par Pedalons (corps JSON avec ce code) prouve que le job n'existe pas
            fail(migrationId, "JOB_LOST", "Pedalons does not know job " + migration.getPedalonsJobId(), response.rawBody());
            return;
        }

        if (response.status() != 200) {
            // reseau, 5xx, secret refuse ou 404 d'un intermediaire (proxy, deploiement en cours) :
            // on reessaie au tick suivant, jusqu'au delai maximal
            log.warn("Pedalons status of job {} unavailable (HTTP {})", migration.getPedalonsJobId(), response.status());
            final String timeout = timeout(migration, migration.getRunningSince(), null, now);
            if (timeout != null) {
                fail(migrationId, "TIMEOUT", timeout, null);
            }
            return;
        }

        final String jobStatus = response.text("status");
        if ("SUCCEEDED".equals(jobStatus)) {
            succeed(migrationId, response);
        } else if ("FAILED".equals(jobStatus)) {
            final JsonNode error = response.body().path("error");
            fail(migrationId,
                    error.hasNonNull("code") ? error.get("code").asText() : "FAILED",
                    error.hasNonNull("message") ? error.get("message").asText() : null,
                    response.rawBody());
        } else {
            final boolean firstRun = "RUNNING".equals(jobStatus) && migration.getRunningSince() == null;
            final String timeout = timeout(migration, firstRun ? now : migration.getRunningSince(), jobStatus, now);
            if (timeout != null) {
                fail(migrationId, "TIMEOUT", timeout, response.rawBody());
                return;
            }
            // progression, affichee par la page de suivi ; conditionnelle : une annulation l'emporte
            tx.executeWithoutResult(status -> {
                migrationRepository.recordProgress(migrationId, response.rawBody(), now);
                if (firstRun) {
                    migrationRepository.markRunningSince(migrationId, now);
                }
            });
        }
    }

    /**
     * Delai depasse pour un job non termine : message d'erreur, ou null. jobStatus est le dernier
     * statut rapporte par Pedalons (null s'il n'a pas repondu). En QUEUED (ou tant que Pedalons n'a
     * jamais rapporte RUNNING), {@link #QUEUED_TIMEOUT} ; sinon {@link #RUNNING_TIMEOUT} depuis le
     * premier RUNNING.
     */
    static String timeout(PedalonsMigration migration, Instant runningSince, String jobStatus, Instant now) {
        final Instant triggeredAt = migration.getTriggeredAt() == null ? migration.getCreatedAt() : migration.getTriggeredAt();
        if (runningSince == null || "QUEUED".equals(jobStatus)) {
            final Instant queuedSince = runningSince == null ? triggeredAt : runningSince;
            return queuedSince.isBefore(now.minus(QUEUED_TIMEOUT))
                    ? "Job still queued after " + QUEUED_TIMEOUT.toHours() + " h"
                    : null;
        }
        return runningSince.isBefore(now.minus(RUNNING_TIMEOUT))
                ? "No terminal status after " + RUNNING_TIMEOUT.toHours() + " h of execution"
                : null;
    }

    /**
     * Job termine avec succes : le bilan (compteurs, avertissements, table d'URL) est enregistre
     * tel quel, rien d'autre ne change - le groupe reste modifiable et non redirige. La table d'URL
     * ne sert qu'a la bascule ({@link #switchTeam}).
     */
    void succeed(String migrationId, PedalonsClient.Response response) {

        final Instant now = clock.instant();
        final JsonNode body = response.body();

        final PedalonsMigration migration = migrationRepository.findById(migrationId).orElse(null);
        if (migration == null || migration.getStatus() != PedalonsMigrationStatus.RUNNING) {
            log.warn("Pedalons migration {} reported SUCCEEDED but is no longer RUNNING (cancelled?): ignored", migrationId);
            return;
        }

        final String teamUrl = teamUrl(body, redirects(migration.getTeamId(), body.path("urlMap")));

        // UPDATE conditionnel : un succes rapporte apres une annulation ne change rien
        final Integer moved = tx.execute(status -> migrationRepository.markSucceeded(migrationId, now, response.rawBody(), teamUrl));

        if (moved == null || moved == 0) {
            log.warn("Pedalons migration {} reported SUCCEEDED but is no longer RUNNING (cancelled?): ignored", migrationId);
        } else {
            log.info("Pedalons migration {} succeeded for team {} ({})", migrationId, migration.getTeamId(), teamUrl);
        }
    }

    /**
     * URL du groupe sur Pedalons d'apres un statut SUCCEEDED : targetTeam.url, a defaut l'entree
     * TEAM de la table d'URL ; null si aucune n'est une URL http(s) valide.
     */
    private static String teamUrl(JsonNode body, List<PedalonsRedirect> redirects) {
        return Optional.ofNullable(validUrl(body.path("targetTeam").path("url").asText(null)))
                .orElseGet(() -> redirects.stream()
                        .filter(r -> PedalonsRedirect.TEAM.equals(r.getEntityType()))
                        .map(PedalonsRedirect::getTargetUrl)
                        .findFirst().orElse(null));
    }

    /**
     * Table d'URL de Pedalons : {TYPE: {idBiketeam: url}}. Les types inconnus et les URL non
     * http(s) sont ecartes : la table ne doit pas pouvoir servir de redirection ouverte.
     */
    static List<PedalonsRedirect> redirects(String teamId, JsonNode urlMap) {
        final List<PedalonsRedirect> result = new ArrayList<>();
        if (urlMap == null || !urlMap.isObject()) {
            return result;
        }
        final Iterator<Map.Entry<String, JsonNode>> types = urlMap.fields();
        while (types.hasNext()) {
            final Map.Entry<String, JsonNode> type = types.next();
            if (!PedalonsRedirect.TYPES.contains(type.getKey()) || !type.getValue().isObject()) {
                continue;
            }
            final Iterator<Map.Entry<String, JsonNode>> entries = type.getValue().fields();
            while (entries.hasNext()) {
                final Map.Entry<String, JsonNode> entry = entries.next();
                final String url = validUrl(entry.getValue().asText(null));
                if (url == null || entry.getKey().isEmpty() || entry.getKey().length() > 255) {
                    log.warn("Ignoring invalid Pedalons redirect {} {}", type.getKey(), entry.getKey());
                    continue;
                }
                result.add(new PedalonsRedirect(teamId, type.getKey(), entry.getKey(), url));
            }
        }
        return result;
    }

    static String validUrl(String url) {
        if (url == null || url.isBlank() || url.length() > 1000) {
            return null;
        }
        try {
            final URI uri = URI.create(url);
            if (uri.getHost() == null || !("https".equals(uri.getScheme()) || "http".equals(uri.getScheme()))) {
                return null;
            }
            return url;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Echec : l'etat terminal est enregistre, rien d'autre ne change (une migration ne gele pas le
     * groupe).
     */
    void fail(String migrationId, String code, String message, String rawBody) {
        fail(migrationId, code, message, rawBody, null);
    }

    private void fail(String migrationId, String code, String message, String rawBody, PedalonsMigrationStatus expected) {
        final Instant now = clock.instant();
        // UPDATE conditionnel : un echec rapporte apres une annulation, ou pour une migration qui
        // n'est plus dans l'etat attendu, est ignore
        final Integer failed = tx.execute(status -> migrationRepository.markFailed(migrationId,
                expected == null ? PedalonsMigrationStatus.ACTIVE : List.of(expected), now, code, message, rawBody));
        if (failed == null || failed == 0) {
            log.info("Pedalons migration {} not failed ({}): already moved on", migrationId, code);
            return;
        }
        log.warn("Pedalons migration {} failed: {} {}", migrationId, code, message == null ? "" : message);
    }

    // ------------------------------------------------------------------------------------------
    // Bascule (administrateur du groupe)
    // ------------------------------------------------------------------------------------------

    /**
     * Bascule du groupe, a partir de sa derniere migration reussie : la table d'URL de son bilan est
     * enregistree et, a partir de maintenant, toutes les URL du groupe redirigent vers Pedalons (le
     * groupe est en lecture seule ici). Cette migration recoit switched_at ; les demandes encore en
     * attente de confirmation sur Pedalons sont annulees (elles viseraient un groupe bascule).
     */
    public String switchTeam(Team team) {

        if (!properties.isEnabled()) {
            throw new PedalonsMigrationException("La migration vers Pédalons n'est pas activée.");
        }

        final Instant now = clock.instant();
        final String teamId = team.getId();

        final Integer count = tx.execute(status -> {
            if (migrationRepository.existsByTeamIdAndStatusIn(teamId, PedalonsMigrationStatus.ACTIVE)) {
                throw new PedalonsMigrationException("Une migration de ce groupe est en cours : attendez sa fin.");
            }
            final PedalonsMigration last = lastSucceeded(teamId)
                    .orElseThrow(() -> new PedalonsMigrationException("Aucune migration réussie : lancez d'abord une migration vers Pédalons."));
            final JsonNode body = readResult(last);
            final List<PedalonsRedirect> redirects = redirects(teamId, body.path("urlMap"));
            final String switchUrl = Optional.ofNullable(validUrl(last.getPedalonsTeamUrl())).orElseGet(() -> teamUrl(body, redirects));
            if (switchUrl == null) {
                throw new PedalonsMigrationException("Le bilan de la dernière migration ne donne pas l'adresse du groupe sur Pédalons : relancez une migration.");
            }
            // conditionnel : rien ne bascule si le groupe l'est deja
            if (teamRepository.switchPedalons(teamId, switchUrl, now) == 0) {
                throw new PedalonsMigrationException("Ce groupe a déjà été migré vers Pédalons.");
            }
            redirectRepository.deleteByTeamId(teamId);
            redirectRepository.saveAll(redirects);
            last.setSwitchedAt(now);
            last.setUpdatedAt(now);
            migrationRepository.save(last);
            for (PedalonsMigration pending : migrationRepository.findByTeamIdAndStatusIn(teamId, List.of(PedalonsMigrationStatus.REQUESTED))) {
                pending.setStatus(PedalonsMigrationStatus.CANCELLED, now);
                migrationRepository.save(pending);
            }
            return redirects.size();
        });

        redirectCache.invalidate();
        log.info("Team {} switched to Pedalons ({} redirects)", teamId, count);
        return "Le groupe a basculé vers Pédalons : ses adresses redirigent désormais vers Pédalons.";
    }

    /** Derniere migration reussie de l'equipe (la plus recemment demandee). */
    private Optional<PedalonsMigration> lastSucceeded(String teamId) {
        return migrationRepository.findByTeamIdOrderByCreatedAtDesc(teamId).stream()
                .filter(m -> m.getStatus() == PedalonsMigrationStatus.SUCCEEDED)
                .findFirst();
    }

    /** Dernier statut recu de Pedalons pour cette migration ; un noeud vide s'il est absent ou illisible. */
    private static JsonNode readResult(PedalonsMigration migration) {
        if (Strings.isBlank(migration.getResultJson())) {
            return JSON.createObjectNode();
        }
        try {
            return JSON.readTree(migration.getResultJson());
        } catch (JsonProcessingException e) {
            return JSON.createObjectNode();
        }
    }

    // ------------------------------------------------------------------------------------------
    // Annulation de la bascule (administrateur de la plateforme)
    // ------------------------------------------------------------------------------------------

    /**
     * Le groupe redevient un groupe biketeam ordinaire : plus de redirection. Une migration active (TRIGGERING, RUNNING) est annulee dans la meme
     * transaction (CANCELLED, {@link PedalonsMigrationRepository#CANCELLED_BY_ADMIN}) : l'export
     * n'est plus servi (le job Pedalons echouera sur l'export) et un succes ou un echec rapporte
     * ensuite est ignore. Les navigateurs qui ont mis un 301 en cache continueront d'aller sur
     * Pedalons.
     */
    public void unswitch(String teamId) {
        final Instant now = clock.instant();
        // la migration d'abord : meme ordre de verrous que succeed() et fail()
        final Integer cancelled = tx.execute(status -> {
            final int count = migrationRepository.cancelActive(teamId, now);
            redirectRepository.deleteByTeamId(teamId);
            teamRepository.updatePedalonsState(teamId, null);
            return count;
        });
        redirectCache.invalidate();
        if (cancelled != null && cancelled > 0) {
            log.warn("Pedalons switch of team {} cancelled, {} active migration(s) cancelled", teamId, cancelled);
        } else {
            log.info("Pedalons switch of team {} cancelled", teamId);
        }
    }

}
