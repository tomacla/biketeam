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
import java.util.Set;

@Repository
public interface TripRepository extends PagingAndSortingRepository<Trip, String> {

    Optional<Trip> findByPermalink(String permalink);

    List<Trip> findAllByDeletionAndTeamIdAndPublishedStatusAndPublishedAtLessThan(boolean deletion, String teamId, PublishedStatus publishedStatus, ZonedDateTime now);

    Page<Trip> findAllByDeletionAndTeamIdInAndStartDateBetweenAndPublishedStatus(boolean deletion, Set<String> teamIds, LocalDate from, LocalDate to, PublishedStatus publishedStatus, Pageable pageable);

    // do not filter by published at (ADMIN)
    List<TripIdTitleDateProjection> findAllByDeletionAndTeamIdOrderByStartDateDesc(boolean deletion, String teamId);

    List<TripIdTitleDateProjection> findAllByDeletion(boolean deletion);

    @Transactional
    @Modifying
    @Query(value = "delete from trip_participant where user_id = :userId", nativeQuery = true)
    void removeParticipant(@Param("userId") String userId);

}
