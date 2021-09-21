package info.tomacla.biketeam.domain.team;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends PagingAndSortingRepository<Team, String>, JpaSpecificationExecutor<Team> {

    List<Team> findAll();

    Page<Team> findAll(Pageable pageable);

    @Query(value = "select team_id as id, domain as domain from team_configuration where domain is not null", nativeQuery = true)
    List<TeamIdDomainProjection> findAllTeamWithDomain();

}
