package info.tomacla.biketeam.domain.pedalons;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedalonsMigrationRepository extends CrudRepository<PedalonsMigration, String> {

    Optional<PedalonsMigration> findByIdAndTeamId(String id, String teamId);

    List<PedalonsMigration> findByTeamIdOrderByCreatedAtDesc(String teamId);

    List<PedalonsMigration> findByTeamIdAndStatusIn(String teamId, Collection<PedalonsMigrationStatus> statuses);

    boolean existsByTeamIdAndStatusIn(String teamId, Collection<PedalonsMigrationStatus> statuses);

    List<PedalonsMigration> findByStatus(PedalonsMigrationStatus status);

    // Transitions de statut : UPDATE conditionnels sur l'etat attendu, jamais un save() d'une copie
    // lue plus tot - une annulation (unswitch) ou un autre declenchement concurrent ne doit pas
    // etre ecrase. Rendent le nombre de lignes modifiees (0 : la migration a change d'etat).
    // Appelees dans une transaction ouverte par PedalonsMigrationService.

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update PedalonsMigration m set m.status = :to, m.updatedAt = :now where m.id = :id and m.status = :from")
    int transition(@Param("id") String id, @Param("from") PedalonsMigrationStatus from,
                   @Param("to") PedalonsMigrationStatus to, @Param("now") Instant now);

    /** TRIGGERING -> RUNNING : le job existe, le grant consomme est oublie. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update PedalonsMigration m set m.status = :running, m.pedalonsJobId = :jobId, m.grantValue = null, m.updatedAt = :now"
            + " where m.id = :id and m.status = :triggering")
    int markRunning(@Param("id") String id, @Param("jobId") String jobId, @Param("now") Instant now,
                    @Param("running") PedalonsMigrationStatus running, @Param("triggering") PedalonsMigrationStatus triggering);

    default int markRunning(String id, String jobId, Instant now) {
        return markRunning(id, jobId, now, PedalonsMigrationStatus.RUNNING, PedalonsMigrationStatus.TRIGGERING);
    }

    /** Progression d'un job RUNNING (dernier statut recu de Pedalons). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update PedalonsMigration m set m.resultJson = :resultJson, m.updatedAt = :now where m.id = :id and m.status = :running")
    int recordProgress(@Param("id") String id, @Param("resultJson") String resultJson, @Param("now") Instant now,
                       @Param("running") PedalonsMigrationStatus running);

    default int recordProgress(String id, String resultJson, Instant now) {
        return recordProgress(id, resultJson, now, PedalonsMigrationStatus.RUNNING);
    }

    /** Premier statut RUNNING rapporte par Pedalons : garde la premiere valeur. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update PedalonsMigration m set m.runningSince = :now where m.id = :id and m.status = :running and m.runningSince is null")
    int markRunningSince(@Param("id") String id, @Param("now") Instant now, @Param("running") PedalonsMigrationStatus running);

    default int markRunningSince(String id, Instant now) {
        return markRunningSince(id, now, PedalonsMigrationStatus.RUNNING);
    }

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update PedalonsMigration m set m.status = :succeeded, m.finishedAt = :now, m.updatedAt = :now,"
            + " m.resultJson = :resultJson, m.pedalonsTeamUrl = :teamUrl where m.id = :id and m.status = :running")
    int markSucceeded(@Param("id") String id, @Param("now") Instant now, @Param("resultJson") String resultJson,
                      @Param("teamUrl") String teamUrl, @Param("succeeded") PedalonsMigrationStatus succeeded,
                      @Param("running") PedalonsMigrationStatus running);

    default int markSucceeded(String id, Instant now, String resultJson, String teamUrl) {
        return markSucceeded(id, now, resultJson, PedalonsMigration.truncate(teamUrl, 1000),
                PedalonsMigrationStatus.SUCCEEDED, PedalonsMigrationStatus.RUNNING);
    }

    /** Echec, seulement depuis l'un des etats attendus ; resultJson n'est remplace que s'il est fourni. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update PedalonsMigration m set m.status = :failed, m.finishedAt = :now, m.updatedAt = :now,"
            + " m.errorCode = :code, m.errorMessage = :message, m.grantValue = null,"
            + " m.resultJson = coalesce(:resultJson, m.resultJson) where m.id = :id and m.status in :expected")
    int markFailed(@Param("id") String id, @Param("expected") Collection<PedalonsMigrationStatus> expected,
                   @Param("now") Instant now, @Param("code") String code, @Param("message") String message,
                   @Param("resultJson") String resultJson, @Param("failed") PedalonsMigrationStatus failed);

    default int markFailed(String id, Collection<PedalonsMigrationStatus> expected, Instant now, String code,
                           String message, String resultJson) {
        return markFailed(id, expected, now, PedalonsMigration.truncate(code, 60), PedalonsMigration.truncate(message, 2000),
                resultJson, PedalonsMigrationStatus.FAILED);
    }

    /**
     * Annulation par l'administrateur de la plateforme des migrations actives (TRIGGERING,
     * RUNNING) d'une equipe : l'export n'est plus servi, un succes ou un echec rapporte ensuite est
     * ignore.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update PedalonsMigration m set m.status = :cancelled, m.finishedAt = :now, m.updatedAt = :now,"
            + " m.errorCode = :code, m.errorMessage = :message, m.grantValue = null"
            + " where m.teamId = :teamId and m.status in :active")
    int cancelActive(@Param("teamId") String teamId, @Param("now") Instant now, @Param("code") String code,
                     @Param("message") String message, @Param("cancelled") PedalonsMigrationStatus cancelled,
                     @Param("active") Collection<PedalonsMigrationStatus> active);

    String CANCELLED_BY_ADMIN = "CANCELLED_BY_ADMIN";

    default int cancelActive(String teamId, Instant now) {
        return cancelActive(teamId, now, CANCELLED_BY_ADMIN, "Annulée par un administrateur de la plateforme",
                PedalonsMigrationStatus.CANCELLED, PedalonsMigrationStatus.ACTIVE);
    }

    /**
     * Appele avant la suppression physique d'une equipe (AsyncDeletionService) : la cle etrangere
     * vers team n'est pas en cascade.
     */
    @Transactional
    @Modifying
    @Query("delete from PedalonsMigration m where m.teamId = :teamId")
    void deleteByTeamId(@Param("teamId") String teamId);

}
