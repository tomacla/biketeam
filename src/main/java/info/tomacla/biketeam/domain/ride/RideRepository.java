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

}
