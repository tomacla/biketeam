package info.tomacla.biketeam.domain.notification;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface NotificationRepository extends PagingAndSortingRepository<Notification, String>, JpaSpecificationExecutor<Notification> {

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
    @Query(value = "delete from notification where (viewed = true and created_at < (NOW() - interval '2 months')) OR (viewed = false AND created_at < (NOW() - interval '6 months'))", nativeQuery = true)
    void deleteOld();

}