package info.tomacla.biketeam.domain.userrole;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRole, String>, PagingAndSortingRepository<UserRole, String>, JpaSpecificationExecutor<UserRole> {

    @Transactional
    @Modifying
    @Query(value = "delete from user_role where user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query(value = "delete from user_role where team_id = :teamId", nativeQuery = true)
    void deleteByTeamId(@Param("teamId") String teamId);

}
