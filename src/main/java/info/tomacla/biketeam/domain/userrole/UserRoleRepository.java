package info.tomacla.biketeam.domain.userrole;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRole, String> {

    List<UserRole> findAll();

    List<UserRole> findByTeam_Id(String teamId);

}
