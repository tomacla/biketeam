package info.tomacla.biketeam.domain.trip;

import info.tomacla.biketeam.common.data.PublishedStatus;
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
import java.util.Optional;

@Repository
public interface TripRepository extends PagingAndSortingRepository<Trip, String> {

    Optional<Trip> findByPermalink(String permalink);

    List<Trip> findAllByTeamIdAndPublishedStatusAndPublishedAtLessThan(String teamId, PublishedStatus publishedStatus, ZonedDateTime now);

    Page<Trip> findByTeamIdAndStartDateBetweenAndPublishedStatus(String teamId, LocalDate from, LocalDate to, PublishedStatus publishedStatus, Pageable pageable);

    // do not filter by published at (ADMIN)
    List<TripIdTitleDateProjection> findAllByTeamIdOrderByStartDateDesc(String teamId);

    @Transactional
    @Modifying
    @Query(value = "delete from trip_participant where user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") String userId);

}
