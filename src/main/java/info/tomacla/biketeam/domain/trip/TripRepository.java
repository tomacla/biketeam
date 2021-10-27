package info.tomacla.biketeam.domain.trip;

import info.tomacla.biketeam.common.PublishedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface TripRepository extends PagingAndSortingRepository<Trip, String> {

    List<Trip> findAllByTeamIdAndPublishedStatusAndPublishedAtLessThan(String teamId, PublishedStatus publishedStatus, ZonedDateTime now);

    Page<Trip> findByTeamIdAndStartDateBetweenAndPublishedStatus(String teamId, LocalDate from, LocalDate to, PublishedStatus publishedStatus, Pageable pageable);

    // do not filter by published at (ADMIN)
    List<TripIdTitleDateProjection> findAllByTeamIdOrderByStartDateDesc(String teamId);

    @Transactional
    @Modifying
    @Query(value = "update trip_stage set map_id = NULL where map_id = :removedMapId", nativeQuery = true)
    void removeMapIdInStages(@Param("removedMapId") String removedMapId);

    @Transactional
    @Modifying
    @Query(value = "update trip_stage set map_id = :newMapId where map_id = :removedMapId", nativeQuery = true)
    void updateMapIdInStages(@Param("removedMapId") String removedMapId, @Param("newMapId") String newMapId);

}
