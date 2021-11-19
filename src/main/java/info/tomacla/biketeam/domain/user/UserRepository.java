package info.tomacla.biketeam.domain.user;

import info.tomacla.biketeam.domain.team.Team;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

    List<User> findAllByOrderByIdAsc();

    Optional<User> findByStravaId(Long stravaId);

    List<User> findByEmailNotNullAndRoles_Team(Team team);

}
