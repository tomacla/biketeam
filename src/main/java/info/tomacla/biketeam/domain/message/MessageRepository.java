package info.tomacla.biketeam.domain.message;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface MessageRepository extends CrudRepository<Message, String>, PagingAndSortingRepository<Message, String>, JpaSpecificationExecutor<Message> {

    @Transactional
    @Modifying
    @Query(value = "delete from message where user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query(value = "delete from message where reply_to_id = :messageId", nativeQuery = true)
    void deleteReplies(@Param("messageId") String messageId);

    @Transactional
    @Modifying
    @Query(value = "delete from message where target_id = :targetId", nativeQuery = true)
    void deleteByTargetId(@Param("targetId") String targetId);


    /**
     * Fusion de comptes : les messages du compte absorbe sont reattribues au compte conserve.
     * Aucune contrainte d'unicite sur message.user_id, un simple update suffit.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update message set user_id = :targetId where user_id = :sourceId", nativeQuery = true)
    int moveToUser(@Param("sourceId") String sourceId, @Param("targetId") String targetId);

}