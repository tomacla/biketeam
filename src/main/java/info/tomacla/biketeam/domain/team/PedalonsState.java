package info.tomacla.biketeam.domain.team;

/**
 * Etat d'une equipe vis-a-vis de sa migration vers Pedalons (colonne team.pedalons_state).
 * <p>
 * null : equipe ordinaire, migree ou non vers Pedalons (une migration ne change rien ici).
 * MIGRATED : bascule faite, toutes les URL de l'equipe redirigent vers Pedalons.
 */
public enum PedalonsState {

    MIGRATED

}
