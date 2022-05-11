package info.tomacla.biketeam.domain.message;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface RideMessageRepository extends CrudRepository<RideMessage, String> {

    List<RideMessage> findAll();

    @Transactional
    @Modifying
    @Query(value = "delete from ride_message where user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") String userId);

}