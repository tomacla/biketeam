package info.tomacla.biketeam.domain.trip;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import jakarta.transaction.Transactional;

@Repository
public interface TripRepository extends CrudRepository<Trip, String>, PagingAndSortingRepository<Trip, String>, JpaSpecificationExecutor<Trip> {

    @Transactional
    @Modifying
    @Query(value = "delete from trip_participant where user_id = :userId", nativeQuery = true)
    void removeParticipant(@Param("userId") String userId);


    /**
     * Fusion de comptes, etape 1 : les sejours ou les DEUX comptes sont deja inscrits.
     * trip_participant n'a ni cle primaire ni index unique : voir
     * {@link info.tomacla.biketeam.domain.ride.RideRepository#deleteDuplicateParticipations}.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "delete from trip_participant where user_id = :sourceId "
            + "and trip_id in (select trip_id from trip_participant where user_id = :targetId)",
            nativeQuery = true)
    int deleteDuplicateParticipations(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

    /**
     * Fusion de comptes, etape 2 : les participations restantes suivent le compte conserve.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update trip_participant set user_id = :targetId where user_id = :sourceId", nativeQuery = true)
    int moveParticipations(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

    // export vers Pedalons (TeamSnapshotBuilder) : graphe de chargement (FETCH) - ce qui est liste
    // vient dans la meme requete, tout le reste (y compris les associations EAGER : participants,
    // inscriptions...) n'est pas charge. Nombre de requetes independant du nombre d'elements.
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"startPlace", "endPlace", "stages", "stages.map"})
    List<Trip> findByTeamId(String teamId);

    long countByTeamIdAndDeletionFalse(String teamId);

    // redirections vers Pedalons (PedalonsRedirectCache) : une requete pour toutes les equipes basculees
    @Query("select new info.tomacla.biketeam.domain.trip.TripKey(t.id, t.teamId, t.permalink, t.deletion) "
            + "from Trip t where t.teamId in :teamIds")
    List<TripKey> findKeysByTeamIdIn(@Param("teamIds") java.util.Collection<String> teamIds);

    @Query("select count(s) from TripStage s where s.trip.teamId = :teamId and s.trip.deletion = false")
    long countStagesOfActiveTrips(@Param("teamId") String teamId);

}
