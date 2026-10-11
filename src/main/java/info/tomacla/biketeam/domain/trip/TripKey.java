package info.tomacla.biketeam.domain.trip;

/**
 * Cles d'adressage d'un voyage (id et permalien), pour la table de redirection vers Pedalons.
 */
public record TripKey(String id, String teamId, String permalink, boolean deletion) {
}
