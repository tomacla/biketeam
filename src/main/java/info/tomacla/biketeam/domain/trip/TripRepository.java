package info.tomacla.biketeam.domain.trip;

import info.tomacla.biketeam.common.data.PublishedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

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

}
