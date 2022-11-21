package info.tomacla.biketeam.domain.notification;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, String> {

    List<Notification> findAll();

    List<Notification> findAllByUserIdAndViewedOrderByCreatedAtDesc(String userId, boolean viewed);

    @Transactional
    @Modifying
    @Query(value = "delete from notification where user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query(value = "delete from notification where element_id = :elementId", nativeQuery = true)
    void deleteByElementId(@Param("elementId") String elementId);

    @Transactional
    @Modifying
    @Query(value = "delete from notification where viewed = true and created_at < (NOW() - interval '2 months')", nativeQuery = true)
    void deleteOldRead();

    @Transactional
    @Modifying
    @Query(value = "delete from notification where created_at < (NOW() - interval '6 months')", nativeQuery = true)
    void deleteOld();

}