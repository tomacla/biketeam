package info.tomacla.biketeam.domain.ride;

import info.tomacla.biketeam.common.PublishedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface RideRepository extends PagingAndSortingRepository<Ride, String> {

    List<Ride> findAllByPublishedStatusAndPublishedAtLessThan(PublishedStatus publishedStatus, ZonedDateTime now);

    Page<Ride> findByDateBetweenAndPublishedStatus(LocalDate from, LocalDate to, PublishedStatus publishedStatus, Pageable pageable);

    // do not filter by published at (ADMIN)
    List<RideIdTitleDateProjection> findAllByOrderByDateDesc();

}
