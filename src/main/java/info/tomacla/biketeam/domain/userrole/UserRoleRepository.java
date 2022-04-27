package info.tomacla.biketeam.domain.userrole;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRole, String> {

    List<UserRole> findAll();

    List<UserRole> findByTeam_Id(String teamId);

    @Transactional
    @Modifying
    @Query(value = "delete from user_role where user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") String userId);

}
