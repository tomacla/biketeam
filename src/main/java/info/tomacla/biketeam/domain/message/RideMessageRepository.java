package info.tomacla.biketeam.domain.message;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideMessageRepository extends CrudRepository<RideMessage, String> {

    List<RideMessage> findAll();

    List<RideMessage> findByTeamId(String teamId);

}