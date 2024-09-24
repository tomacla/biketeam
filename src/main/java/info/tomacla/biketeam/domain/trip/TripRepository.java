package info.tomacla.biketeam.domain.trip;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface TripRepository extends CrudRepository<Trip, String>, PagingAndSortingRepository<Trip, String>, JpaSpecificationExecutor<Trip> {

    @Transactional
    @Modifying
    @Query(value = "delete from trip_participant where user_id = :userId", nativeQuery = true)
    void removeParticipant(@Param("userId") String userId);

}
