package info.tomacla.biketeam.domain.pedalons;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedalonsRedirectRepository extends CrudRepository<PedalonsRedirect, PedalonsRedirect.Key> {

    List<PedalonsRedirect> findByTeamId(String teamId);

    List<PedalonsRedirect> findByTeamIdIn(java.util.Collection<String> teamIds);

    // appele dans une transaction ouverte par le service (TransactionTemplate)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PedalonsRedirect r where r.teamId = :teamId")
    int deleteByTeamId(@Param("teamId") String teamId);

}
