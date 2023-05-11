package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.domain.userrole.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TeamRepository extends PagingAndSortingRepository<Team, String>, JpaSpecificationExecutor<Team> {

    List<Team> findAllByDeletion(boolean deletion);

    Page<Team> findAllByDeletion(boolean deletion, Pageable pageable);

    Page<Team> findAllByDeletionAndVisibilityIn(boolean deletion, List<Visibility> visibility, Pageable pageable);

    List<Team> findAllByDeletionAndRoles_UserIdAndRoles_RoleIn(boolean deletion, String userId, Set<Role> roles);

}
