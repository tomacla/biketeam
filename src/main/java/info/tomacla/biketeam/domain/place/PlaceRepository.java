package info.tomacla.biketeam.domain.place;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface PlaceRepository extends CrudRepository<Place, String>, PagingAndSortingRepository<Place, String>, JpaSpecificationExecutor<Place> {

    @Transactional
    @Modifying
    @Query(value = "update ride set start_place_id = NULL where start_place_id = :removedPlaceId", nativeQuery = true)
    void removeStartPlaceIdInRide(@Param("removedPlaceId") String removedPlaceId);

    @Transactional
    @Modifying
    @Query(value = "update ride set end_place_id = NULL where end_place_id = :removedPlaceId", nativeQuery = true)
    void removeEndPlaceIdInRide(@Param("removedPlaceId") String removedPlaceId);

    @Transactional
    @Modifying
    @Query(value = "update trip set start_place_id = NULL where start_place_id = :removedPlaceId", nativeQuery = true)
    void removeStartPlaceIdInTrip(@Param("removedPlaceId") String removedPlaceId);

    @Transactional
    @Modifying
    @Query(value = "update trip set end_place_id = NULL where end_place_id = :removedPlaceId", nativeQuery = true)
    void removeEndPlaceIdInTrip(@Param("removedPlaceId") String removedPlaceId);

    @Transactional
    @Modifying
    @Query(value = "update ride_template set start_place_id = NULL where start_place_id = :removedPlaceId", nativeQuery = true)
    void removeStartPlaceIdInRideTemplate(@Param("removedPlaceId") String removedPlaceId);

    @Transactional
    @Modifying
    @Query(value = "update ride_template set end_place_id = NULL where end_place_id = :removedPlaceId", nativeQuery = true)
    void removeEndPlaceIdInRideTemplate(@Param("removedPlaceId") String removedPlaceId);

}