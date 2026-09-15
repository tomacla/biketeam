package info.tomacla.biketeam.domain.user;

public enum UserAuthTokenType {
    EMAIL_VERIFICATION,
    PASSWORD_RESET,
    /**
     * Rattachement d'un compte incomplet (typiquement Strava) a un compte existant dont
     * l'adresse email vient d'etre revendiquee. Le token porte les DEUX comptes :
     * {@code userId} est le compte demandeur, {@code relatedUserId} le compte destinataire.
     */
    ACCOUNT_MERGE
}
