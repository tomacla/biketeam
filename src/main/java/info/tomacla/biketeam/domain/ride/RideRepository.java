package info.tomacla.biketeam.domain.ride;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface RideRepository extends CrudRepository<Ride, String> {

    List<Ride> findAllByPublishedAtLessThan(ZonedDateTime now);

    List<Ride> findByDateBetweenAndPublishedAtLessThanOrderByDateDesc(LocalDate from, LocalDate to, ZonedDateTime now);

    // do not filter by published at (ADMIN)
    List<RideIdTitleDateProjection> findAllByOrderByDateDesc();

}
