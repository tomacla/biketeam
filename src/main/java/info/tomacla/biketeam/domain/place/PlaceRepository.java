package info.tomacla.biketeam.domain.place;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface PlaceRepository extends CrudRepository<Place, String> {

    List<Place> findAllByTeamIdOrderByNameAsc(String teamId);

    @Query(value = "select p.id as id, p.team_id as teamId, p.name as name,  " +
            "MAX(t1.start_date) as lastTripStartPlaceAppearance, count(distinct t1.id) as tripStartPlaceAppearances, " +
            "MAX(t2.start_date) as lastTripEndPlaceAppearance, count(distinct t2.id) as tripEndPlaceAppearances, " +
            "MAX(r1.date) as lastRideStartPlaceAppearance, count(distinct r1.id) as rideStartPlaceAppearances, " +
            "MAX(r2.date) as lastRideEndPlaceAppearance, count(distinct r2.id) as rideEndPlaceAppearances " +
            "from place p  " +
            "left outer join trip t1 on p.id = t1.start_place_id " +
            "left outer join trip t2 on p.id = t2.end_place_id   " +
            "left outer join ride r1 on p.id = r1.start_place_id " +
            "left outer join ride r2 on p.id = r2.end_place_id   " +
            "where p.team_id = :teamId " +
            "group by p.id, p.team_id, p.name ", nativeQuery = true)
    List<PlaceAppearanceProjection> findAllByTeamIdWithAppearances(@Param("teamId") String teamId);

    @Transactional
    @Modifying
    @Query(value = "delete from place where team_id = :teamId", nativeQuery = true)
    void deleteByTeamId(@Param("teamId") String teamId);

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