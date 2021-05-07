package info.tomacla.biketeam.domain.ride;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface RideRepository extends PagingAndSortingRepository<Ride, String> {

    List<Ride> findAllByPublishedAtLessThan(ZonedDateTime now);

    Page<Ride> findByDateBetweenAndPublishedAtLessThan(LocalDate from, LocalDate to, ZonedDateTime now, Pageable pageable);

    // do not filter by published at (ADMIN)
    List<RideIdTitleDateProjection> findAllByOrderByDateDesc();

}
