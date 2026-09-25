package info.tomacla.biketeam.service.pedalons;

import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.pedalons.PedalonsRedirect;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.PedalonsState;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Decide du sort d'une requete visant une equipe gelee ou basculee vers Pedalons.
 * <p>
 * Seules les equipes du cache (MIGRATING, READY ou MIGRATED) sont testees, sur le premier segment du
 * chemin en minuscules : aucune collision possible avec les routes globales. Les identifiants
 * ou permaliens sont d'abord cherches tels quels dans la table d'URL, puis resolus par les
 * services existants (qui acceptent id ou permalien) - uniquement pour une equipe basculee. Les
 * voyages du catalogue, eux, sont resolus sans SQL par le cache. Tout ce qui ne resout pas retombe
 * sur la page d'equipe Pedalons.
 */
@Component
public class PedalonsRedirectResolver {

    public enum Action {
        /** Requete servie normalement. */
        PASS,
        /** Equipe basculee : redirection 301/302 (lecture) ou 410 (ecriture) vers l'URL. */
        REDIRECT,
        /**
         * Equipe basculee, requete d'API (/api/teams/{t}/**, lecture comprise) : 410 JSON portant
         * l'URL Pedalons de l'equipe - un client d'API ne sait que faire d'une page HTML.
         */
        API_GONE,
        /**
         * Equipe gelee pendant une migration definitive : 423 (page HTML), avec l'URL Pedalons de
         * l'equipe si elle est deja connue (null sinon) et l'etat du gel (MIGRATING ou READY).
         */
        LOCKED,
        /**
         * Equipe gelee, ecriture d'API (/api/teams/{t}/**) : 423 JSON portant l'URL Pedalons de
         * l'equipe si elle est deja connue (null sinon).
         */
        API_LOCKED
    }

    /**
     * @param state etat de l'equipe gelee, pour le texte de la page 423 (LOCKED seulement, null sinon)
     */
    public record Decision(Action action, String url, PedalonsState state) {

        static final Decision PASS = new Decision(Action.PASS, null);

        public Decision(Action action, String url) {
            this(action, url, null);
        }

        static Decision locked(PedalonsRedirectCache.TeamRedirects team) {
            return new Decision(Action.LOCKED, team.fallback().orElse(null), team.state());
        }

        static Decision redirect(String url) {
            return new Decision(Action.REDIRECT, url);
        }
    }

    // ecritures en GET de biketeam
    private static final Set<String> WRITE_SEGMENTS = Set.of(
            "add-participant", "remove-participant", "remove-message", "add-favorite", "remove-favorite");

    private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private final PedalonsRedirectCache cache;
    private final MapService mapService;
    private final RideService rideService;
    private final TripService tripService;

    @Autowired
    public PedalonsRedirectResolver(PedalonsRedirectCache cache, MapService mapService, RideService rideService,
                                    TripService tripService) {
        this.cache = cache;
        this.mapService = mapService;
        this.rideService = rideService;
        this.tripService = tripService;
    }

    /**
     * @param path chemin DECODE dans l'application (voir PedalonsRedirectFilter) : les segments
     *             sont compares tels quels aux identifiants et permaliens, qui peuvent contenir des
     *             caracteres non ASCII (Boucle_de_l’Erdre).
     */
    public Decision resolve(String method, String path) {

        if (path == null || path.startsWith("/internal/pedalons/") || cache.isEmpty()) {
            return Decision.PASS;
        }

        final List<String> segments = Arrays.stream(path.split("/")).filter(s -> !s.isEmpty()).toList();
        if (segments.isEmpty()) {
            return Decision.PASS;
        }

        // /catalog/trips/{tripId} : voyage d'une equipe basculee
        if (segments.size() >= 3 && segments.get(0).equals("catalog") && segments.get(1).equals("trips")) {
            return resolveCatalogTrip(segments.get(2));
        }

        final boolean api = segments.size() >= 3 && segments.get(0).equals("api") && segments.get(1).equals("teams");
        final String teamSegment = api ? segments.get(2) : segments.get(0);
        final List<String> rest = segments.subList(api ? 3 : 1, segments.size());

        final Optional<PedalonsRedirectCache.TeamRedirects> optionalTeam = cache.get(teamSegment);
        if (optionalTeam.isEmpty()) {
            return Decision.PASS;
        }
        final PedalonsRedirectCache.TeamRedirects team = optionalTeam.get();

        // la page de suivi de la migration reste accessible a l'administrateur, gel ou bascule
        if (!api && isMigrationAdminPage(rest)) {
            return Decision.PASS;
        }

        // le logo du groupe reste servi : les listes biketeam, ou le groupe bascule reste affiche, l'utilisent en <img>
        if (!api && rest.size() == 1 && rest.get(0).equals("image")) {
            return Decision.PASS;
        }

        if (team.isFrozen()) {
            if (!isWrite(method, rest, api)) {
                return Decision.PASS;
            }
            return api ? new Decision(Action.API_LOCKED, team.fallback().orElse(null)) : Decision.locked(team);
        }

        if (!team.isMigrated()) {
            return Decision.PASS;
        }

        if (api) {
            return new Decision(Action.API_GONE, team.fallback().orElse(null));
        }

        return target(team, rest)
                .or(team::fallback)
                .map(Decision::redirect)
                .orElse(Decision.PASS);
    }

    /**
     * /catalog/trips/{x} ne nomme pas l'equipe : resolu uniquement par les tables en memoire du
     * cache (ids et permaliens des voyages des equipes basculees), sans SQL par requete - le
     * filtre passe avant Spring Security, sur toutes les requetes du catalogue.
     */
    private Decision resolveCatalogTrip(String tripIdOrPermalink) {
        if (!cache.hasMigrated()) {
            return Decision.PASS;
        }
        return cache.migratedTrip(tripIdOrPermalink)
                .flatMap(trip -> cache.get(trip.teamId())
                        .filter(PedalonsRedirectCache.TeamRedirects::isMigrated)
                        .flatMap(team -> team.url(PedalonsRedirect.TRIP, trip.tripId()).or(team::fallback)))
                .map(Decision::redirect)
                .orElse(Decision.PASS);
    }

    private Optional<String> target(PedalonsRedirectCache.TeamRedirects team, List<String> rest) {

        final String teamId = team.teamId();

        if (rest.isEmpty()) {
            return team.url(PedalonsRedirect.TEAM, teamId);
        }

        final String section = rest.get(0);
        final String key = rest.size() >= 2 ? rest.get(1) : null;

        switch (section) {
            case "faq":
                return team.url(PedalonsRedirect.TEAM_FAQ, teamId).or(() -> team.url(PedalonsRedirect.TEAM_ABOUT, teamId));
            case "maps":
                if (key == null || key.equals("android") || key.equals("garmin") || key.equals("autocomplete") || key.equals("tags")) {
                    return team.url(PedalonsRedirect.ROUTES_LIST, teamId);
                }
                return team.url(PedalonsRedirect.ROUTE, key)
                        .or(() -> mapService.get(teamId, key).map(Map::getId).flatMap(id -> team.url(PedalonsRedirect.ROUTE, id)));
            case "rides":
                if (key == null) {
                    return team.url(PedalonsRedirect.TEAM, teamId);
                }
                return team.url(PedalonsRedirect.RIDE, key)
                        .or(() -> rideService.get(teamId, key).map(Ride::getId).flatMap(id -> team.url(PedalonsRedirect.RIDE, id)));
            case "trips":
                if (key == null) {
                    return team.url(PedalonsRedirect.TEAM, teamId);
                }
                return team.url(PedalonsRedirect.TRIP, key)
                        .or(() -> tripService.get(teamId, key).map(Trip::getId).flatMap(id -> team.url(PedalonsRedirect.TRIP, id)));
            case "publications":
                // les publications n'ont pas de permalien
                return key == null ? Optional.empty() : team.url(PedalonsRedirect.POST, key);
            default:
                return Optional.empty();
        }
    }

    private static boolean isMigrationAdminPage(List<String> rest) {
        return rest.size() >= 2 && rest.get(0).equals("admin") && rest.get(1).equals("pedalons");
    }

    /**
     * Ecritures a refuser pendant le gel : toute methode autre que GET/HEAD, et les ecritures
     * que biketeam fait en GET (adhesion, participation, messages, favoris, administration).
     */
    static boolean isWrite(String method, List<String> rest, boolean api) {
        if (method == null || !READ_METHODS.contains(method.toUpperCase())) {
            return true;
        }
        if (api) {
            return false;
        }
        if (!rest.isEmpty()) {
            final String first = rest.get(0);
            if (rest.size() == 1 && (first.equals("join") || first.equals("leave"))) {
                return true;
            }
            if (first.equals("admin")) {
                return true;
            }
        }
        return rest.stream().anyMatch(WRITE_SEGMENTS::contains);
    }

}
