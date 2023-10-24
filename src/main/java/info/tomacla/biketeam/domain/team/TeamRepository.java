package info.tomacla.biketeam.domain.team;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface TeamRepository extends PagingAndSortingRepository<Team, String>, JpaSpecificationExecutor<Team> {

    @Query(value = "select t.id, max(r.published_at) as lastRidePublishedAt, max(p.published_at) as lastPublicationPublishedAt, max(tr.published_at) as lastTripPublishedAt from team t left outer join ride r on r.team_id = t.id left outer join publication p on p.team_id = t.id left outer join trip tr on tr.team_id = t.id where t.id in :teamIds group by t.id", nativeQuery = true)
    List<LastTeamData> findLastData(@Param("teamIds") Set<String> teamIds);

}
