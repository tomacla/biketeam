package info.tomacla.biketeam.domain.message;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface MessageRepository extends PagingAndSortingRepository<Message, String>, JpaSpecificationExecutor<Message> {

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

}