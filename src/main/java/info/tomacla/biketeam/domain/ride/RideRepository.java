package info.tomacla.biketeam.domain.ride;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RideRepository extends CrudRepository<Ride, String>, PagingAndSortingRepository<Ride, String>, JpaSpecificationExecutor<Ride> {

    @Transactional
    @Modifying
    @Query(value = "delete from ride_group_participant where user_id = :userId", nativeQuery = true)
    void removeParticipant(@Param("userId") String userId);


    /**
     * Fusion de comptes, etape 1 : les groupes ou les DEUX comptes sont deja inscrits.
     * ride_group_participant n'a ni cle primaire ni index unique : sans ce nettoyage prealable,
     * le deplacement creerait une ligne en double et la personne apparaitrait deux fois dans la
     * liste des participants.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "delete from ride_group_participant where user_id = :sourceId "
            + "and ride_group_id in (select ride_group_id from ride_group_participant where user_id = :targetId)",
            nativeQuery = true)
    int deleteDuplicateParticipations(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

    /**
     * Fusion de comptes, etape 2 : les participations restantes suivent le compte conserve.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update ride_group_participant set user_id = :targetId where user_id = :sourceId", nativeQuery = true)
    int moveParticipations(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

}
