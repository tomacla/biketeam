package info.tomacla.biketeam.domain.map;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MapRatingRepository extends CrudRepository<MapRating, String> {

    Optional<MapRating> findByMapIdAndUserId(String mapId, String userId);

    @Query("SELECT AVG(mr.rating) FROM MapRating mr WHERE mr.map.id = :mapId")
    Double findAverageRatingByMapId(@Param("mapId") String mapId);

    @Query("SELECT COUNT(mr) FROM MapRating mr WHERE mr.map.id = :mapId")
    Long countRatingsByMapId(@Param("mapId") String mapId);

    /**
     * Cartes notees par l'un ou l'autre des deux comptes d'une fusion. Releve AVANT de toucher
     * aux notes : les compteurs denormalises map.average_rating / map.rating_count devront etre
     * recalcules sur chacune d'elles, sinon la moyenne derive silencieusement des qu'un doublon
     * est supprime.
     */
    @Query(value = "select distinct map_id from map_rating where user_id in (:sourceId, :targetId)", nativeQuery = true)
    List<String> findRatedMapIdsByUsers(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

    /**
     * Fusion de comptes, etape 1 : sur une carte notee par les deux comptes, la note la plus
     * recente l'emporte. C'est la derniere intention exprimee par la personne, qui est la meme
     * des deux cotes.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update map_rating tgt set rating = src.rating, updated_at = src.updated_at "
            + "from map_rating src "
            + "where src.user_id = :sourceId and tgt.user_id = :targetId "
            + "and tgt.map_id = src.map_id and src.updated_at > tgt.updated_at",
            nativeQuery = true)
    int applyMostRecentRating(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

    /**
     * Fusion de comptes, etape 2 : les doublons restants de la source disparaissent
     * (uk_map_rating_map_user interdit deux notes du meme compte sur la meme carte).
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "delete from map_rating where user_id = :sourceId "
            + "and map_id in (select map_id from map_rating where user_id = :targetId)",
            nativeQuery = true)
    int deleteDuplicateRatings(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

    /**
     * Fusion de comptes, etape 3 : les notes restantes suivent le compte conserve.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update map_rating set user_id = :targetId where user_id = :sourceId", nativeQuery = true)
    int moveRatings(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

}