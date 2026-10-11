package info.tomacla.biketeam.domain.team;

/**
 * Etat d'une equipe vis-a-vis de sa migration vers Pedalons (colonne team.pedalons_state).
 * <p>
 * null : equipe ordinaire. MIGRATING : migration definitive en cours, l'equipe est gelee en
 * lecture seule. READY : migration definitive reussie, table d'URL enregistree, l'equipe attend
 * que son administrateur clique sur « Basculer vers Pedalons » - toujours gelee, pas encore redirigee.
 * MIGRATED : bascule faite, toutes les URL de l'equipe redirigent vers Pedalons.
 */
public enum PedalonsState {

    MIGRATING, READY, MIGRATED

}
