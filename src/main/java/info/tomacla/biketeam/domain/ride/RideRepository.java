package info.tomacla.biketeam.domain.ride;

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
public interface RideRepository extends PagingAndSortingRepository<Ride, String> {

    List<Ride> findAllByTeamIdAndPublishedStatusAndPublishedAtLessThan(String teamId, PublishedStatus publishedStatus, ZonedDateTime now);

    Page<Ride> findByTeamIdAndDateBetweenAndPublishedStatus(String teamId, LocalDate from, LocalDate to, PublishedStatus publishedStatus, Pageable pageable);

    // do not filter by published at (ADMIN)
    List<RideIdTitleDateProjection> findAllByTeamIdOrderByDateDesc(String teamId);

    @Transactional
    @Modifying
    @Query(value = "update ride_group set map_id = NULL where map_id = :removedMapId", nativeQuery = true)
    void removeMapIdInGroups(@Param("removedMapId") String removedMapId);

}
