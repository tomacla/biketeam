package info.tomacla.biketeam.domain.ride;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RideRepository extends CrudRepository<Ride, String> {

    List<Ride> findAll();

    List<Ride> findByDateBetweenOrderByDateDesc(LocalDate from, LocalDate to);

    List<RideIdTitleDateProjection> findAllByOrderByDateDesc();

}
