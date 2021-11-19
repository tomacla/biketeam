package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.domain.userrole.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TeamRepository extends PagingAndSortingRepository<Team, String>, JpaSpecificationExecutor<Team> {

    List<Team> findAll();

    Page<Team> findAll(Pageable pageable);

    Page<Team> findByVisibilityIn(List<Visibility> visibility, Pageable pageable);

    List<Team> findByRoles_UserIdAndRoles_RoleIn(String userId, Set<Role> roles);

    @Query(value = "select team_id as id, domain as domain from team_configuration where domain is not null", nativeQuery = true)
    List<TeamProjection> findAllTeamWithDomain();

}
