package info.tomacla.biketeam.domain.pedalons;

import java.util.List;

/**
 * Cycle de vie d'une demande de migration vers Pedalons.
 *
 * <pre>
 * REQUESTED --callback outcome=cancelled--> CANCELLED
 *     |  (nouvelle demande pour l'equipe, ou plus de 60 min) --> CANCELLED / EXPIRED
 *     | callback grant
 *     v
 * TRIGGERING --202/200--> RUNNING --statut SUCCEEDED--> SUCCEEDED
 *     |                     |
 *     | refus / 3 echecs    +--statut FAILED, 404, 6 h depuis le 1er RUNNING, 48 h en QUEUED--> FAILED
 *     v                     |
 *   FAILED                  +--annulation par l'admin plateforme (unswitch)--> CANCELLED
 * </pre>
 * TRIGGERING est annule de meme par un unswitch. Toute transition est un UPDATE conditionnel sur
 * l'etat attendu : un succes ou un echec rapporte apres une annulation est ignore.
 */
public enum PedalonsMigrationStatus {

    REQUESTED, CANCELLED, EXPIRED, TRIGGERING, RUNNING, SUCCEEDED, FAILED;

    /**
     * Etats pendant lesquels Pedalons peut lire l'export de l'equipe, et pendant lesquels aucune
     * autre demande ne peut etre lancee.
     */
    public static final List<PedalonsMigrationStatus> ACTIVE = List.of(TRIGGERING, RUNNING);

    public boolean isActive() {
        return ACTIVE.contains(this);
    }

    public boolean isTerminal() {
        return this == CANCELLED || this == EXPIRED || this == SUCCEEDED || this == FAILED;
    }

}
