package info.tomacla.biketeam.domain.message;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface MessageRepository extends CrudRepository<Message, String> {

    List<Message> findAll();

    List<Message> findAllByTargetIdAndTypeOrderByPublishedAtAsc(String targetId, MessageTargetType type);

    List<Message> findAllByUserId(String userId);

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