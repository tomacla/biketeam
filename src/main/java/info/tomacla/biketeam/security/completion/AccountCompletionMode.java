package info.tomacla.biketeam.security.completion;

/**
 * Niveau d'insistance sur la completion de compte, pilote par {@code auth.completion.mode}.
 * <p>
 * Les trois valeurs forment une escalade volontairement progressive : on ouvre en {@link #SUGGESTED}
 * le temps que les comptes Strava se completent d'eux-memes, puis on passe en {@link #ENFORCED}
 * pour les retardataires. {@link #OFF} est le retour arriere.
 */
public enum AccountCompletionMode {

    /**
     * Aucune sollicitation : ni bandeau, ni badge, ni redirection.
     */
    OFF,

    /**
     * Incitation seule : bandeau sur toutes les pages et badge dans la barre de navigation,
     * mais la navigation reste entierement libre.
     */
    SUGGESTED,

    /**
     * Incitation ET blocage : en plus du bandeau, toute page hors whitelist renvoie vers
     * /account/complete.
     */
    ENFORCED

}
