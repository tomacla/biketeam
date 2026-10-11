package info.tomacla.biketeam.service.pedalons;

import info.tomacla.biketeam.domain.pedalons.PedalonsRedirect;
import info.tomacla.biketeam.domain.pedalons.PedalonsRedirectRepository;
import info.tomacla.biketeam.domain.team.PedalonsState;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import info.tomacla.biketeam.domain.trip.TripKey;
import info.tomacla.biketeam.domain.trip.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cache memoire des equipes gelees (MIGRATING, READY) ou basculees (MIGRATED) et de leur table
 * d'URL, lu a chaque requete par le filtre de redirection.
 * <p>
 * Charge paresseusement au premier acces, invalide a chaque changement d'etat (gel, degel,
 * bascule, annulation). Instance unique : pas de diffusion entre noeuds.
 * <p>
 * Le chargement fait un nombre fixe de requetes, quel que soit le nombre d'equipes : les equipes,
 * les tables d'URL des equipes basculees, les cles (id, permalien) de leurs voyages - ces
 * dernieres pour /catalog/trips/{x}, qui ne nomme pas l'equipe et doit etre resolu sans SQL.
 */
@Component
public class PedalonsRedirectCache {

    private static final Logger log = LoggerFactory.getLogger(PedalonsRedirectCache.class);

    /**
     * Etat d'une equipe et sa table (type -> id biketeam -> URL Pedalons).
     */
    public record TeamRedirects(String teamId, PedalonsState state, String pedalonsUrl,
                                Map<String, Map<String, String>> table) {

        public Optional<String> url(String type, String entityId) {
            if (entityId == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(table.getOrDefault(type, Map.of()).get(entityId));
        }

        /**
         * Repli : la page d'equipe Pedalons de la table, sinon team.pedalons_url.
         */
        public Optional<String> fallback() {
            return url(PedalonsRedirect.TEAM, teamId).or(() -> Optional.ofNullable(pedalonsUrl));
        }

        public boolean isMigrated() {
            return state == PedalonsState.MIGRATED;
        }

        /**
         * Gelee en lecture seule : migration definitive en cours, ou reussie et en attente du clic
         * « Basculer » - rien de ce qui serait modifie d'ici la n'arriverait sur Pedalons.
         */
        public boolean isFrozen() {
            return state == PedalonsState.MIGRATING || state == PedalonsState.READY;
        }
    }

    private final TeamRepository teamRepository;
    private final PedalonsRedirectRepository redirectRepository;

    /**
     * Voyage d'une equipe basculee : son equipe et son id (la cle de la table d'URL).
     */
    public record TripRef(String teamId, String tripId) {
    }

    private record State(Map<String, TeamRedirects> teams, boolean anyMigrated, Map<String, TripRef> trips) {
    }

    private final TripRepository tripRepository;

    private volatile State state;

    @Autowired
    public PedalonsRedirectCache(TeamRepository teamRepository, PedalonsRedirectRepository redirectRepository,
                                 TripRepository tripRepository) {
        this.teamRepository = teamRepository;
        this.redirectRepository = redirectRepository;
        this.tripRepository = tripRepository;
    }

    public Optional<TeamRedirects> get(String teamId) {
        if (teamId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(teams().get(teamId.toLowerCase()));
    }

    public boolean isEmpty() {
        return teams().isEmpty();
    }

    public Collection<TeamRedirects> all() {
        return teams().values();
    }

    /**
     * Au moins une equipe basculee : sinon aucune redirection de voyage du catalogue n'est possible.
     */
    public boolean hasMigrated() {
        return state().anyMigrated();
    }

    /**
     * Voyage d'une equipe basculee, par id (tout voyage) ou permalien (voyage non supprime), sans SQL.
     */
    public Optional<TripRef> migratedTrip(String tripIdOrPermalink) {
        if (tripIdOrPermalink == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(state().trips().get(tripIdOrPermalink));
    }

    /**
     * A appeler apres la validation de la transaction qui a change l'etat d'une equipe.
     * <p>
     * Synchronise sur le meme moniteur que le chargement : un chargement commence avant la
     * validation (qui a donc pu lire l'etat precedent) se termine avant l'invalidation, et ne peut
     * pas republier son resultat perime par-dessus elle.
     */
    public synchronized void invalidate() {
        this.state = null;
    }

    private Map<String, TeamRedirects> teams() {
        return state().teams();
    }

    private State state() {
        State current = this.state;
        if (current == null) {
            synchronized (this) {
                current = this.state;
                if (current == null) {
                    current = load();
                    this.state = current;
                }
            }
        }
        return current;
    }

    private State load() {
        final List<Team> teams = teamRepository.findByPedalonsStateIsNotNull();
        final List<String> migratedIds = teams.stream()
                .filter(team -> team.getPedalonsState() == PedalonsState.MIGRATED)
                .map(Team::getId)
                .toList();

        // tables d'URL des equipes basculees : une seule requete
        final Map<String, Map<String, Map<String, String>>> tables = new HashMap<>();
        final Map<String, TripRef> trips = new HashMap<>();
        if (!migratedIds.isEmpty()) {
            for (PedalonsRedirect redirect : redirectRepository.findByTeamIdIn(migratedIds)) {
                tables.computeIfAbsent(redirect.getTeamId(), k -> new HashMap<>())
                        .computeIfAbsent(redirect.getEntityType(), k -> new HashMap<>())
                        .put(redirect.getEntityId(), redirect.getTargetUrl());
                if (PedalonsRedirect.TRIP.equals(redirect.getEntityType())) {
                    trips.putIfAbsent(redirect.getEntityId(), new TripRef(redirect.getTeamId(), redirect.getEntityId()));
                }
            }
            // voyages des equipes basculees : une seule requete ; permalien seulement s'il est actif,
            // comme TripService.findByPermalink
            for (TripKey trip : tripRepository.findKeysByTeamIdIn(migratedIds)) {
                final TripRef ref = new TripRef(trip.teamId(), trip.id());
                trips.putIfAbsent(trip.id(), ref);
                if (!trip.deletion() && trip.permalink() != null && !trip.permalink().isBlank()) {
                    trips.putIfAbsent(trip.permalink(), ref);
                }
            }
        }

        final Map<String, TeamRedirects> result = new HashMap<>();
        for (Team team : teams) {
            result.put(team.getId().toLowerCase(), new TeamRedirects(team.getId(), team.getPedalonsState(),
                    team.getPedalonsUrl(), tables.getOrDefault(team.getId(), Map.of())));
        }
        log.info("Pedalons redirect cache loaded: {} team(s), {} migrated, {} trip key(s)",
                result.size(), migratedIds.size(), trips.size());
        return new State(Map.copyOf(result), !migratedIds.isEmpty(), Map.copyOf(trips));
    }

}
