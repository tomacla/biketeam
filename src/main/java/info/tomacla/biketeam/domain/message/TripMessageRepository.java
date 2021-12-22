package info.tomacla.biketeam.domain.message;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripMessageRepository extends CrudRepository<TripMessage, String> {

    List<TripMessage> findAll();

    List<TripMessage> findByTeamId(String teamId);

}