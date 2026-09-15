package info.tomacla.biketeam.service.merge;

/**
 * Compte rendu d'une fusion de comptes.
 * <p>
 * Sert au journal, aux assertions de test (mesurer ce qui a bouge sans demonter le SQL) et a
 * l'ecran de confirmation, qui doit pouvoir annoncer honnetement ce qui a ete repris.
 *
 * @param privateTeamLost vrai si le compte absorbe possedait une equipe privee que le compte
 *                        conserve n'a pas pu reprendre (il en avait deja une). Cette equipe et
 *                        tout son contenu seront supprimes par la purge asynchrone : ce cas doit
 *                        etre annonce a l'utilisateur AVANT la fusion, jamais constate apres.
 */
public record MergeReport(int trips,
                          int rideGroups,
                          int mapFavorites,
                          int mapRatings,
                          int messages,
                          int notifications,
                          int roles,
                          boolean imageMoved,
                          boolean privateTeamLost) {
}
