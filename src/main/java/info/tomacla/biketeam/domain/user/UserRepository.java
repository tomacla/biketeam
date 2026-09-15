package info.tomacla.biketeam.domain.user;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String>, PagingAndSortingRepository<User, String>, JpaSpecificationExecutor<User> {

    /**
     * Fusion de comptes, etape 1 : les cartes deja en favori sur les DEUX comptes.
     * map_favorite a pour cle primaire le couple (map_id, user_id) : sans ce nettoyage, le
     * deplacement violerait la cle primaire et ferait echouer toute la fusion.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "delete from map_favorite where user_id = :sourceId "
            + "and map_id in (select map_id from map_favorite where user_id = :targetId)",
            nativeQuery = true)
    int deleteDuplicateMapFavorites(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

    /**
     * Fusion de comptes, etape 2 : les favoris restants suivent le compte conserve.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update map_favorite set user_id = :targetId where user_id = :sourceId", nativeQuery = true)
    int moveMapFavorites(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

    /**
     * Suppression de compte : map_favorite est la SEULE table enfant dont la cle etrangere vers
     * user_account n'a pas de ON DELETE CASCADE (voir fk_map_favorite_user_id dans schema-1.0.xml).
     * Sans ce nettoyage explicite, la suppression physique d'un compte ayant des favoris echoue,
     * et comme AsyncDeletionService.performEffectiveDeletion enveloppe toute sa boucle dans un
     * seul try/catch, c'est la purge entiere (cartes, publications, sorties, sejours, equipes)
     * qui s'interrompt.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "delete from map_favorite where user_id = :userId", nativeQuery = true)
    int deleteMapFavorites(@Param("userId") String userId);

}
