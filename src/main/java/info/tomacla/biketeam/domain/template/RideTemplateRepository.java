package info.tomacla.biketeam.domain.template;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideTemplateRepository extends PagingAndSortingRepository<RideTemplate, String> {

    // do not filter by published at (ADMIN)
    List<RideTemplateProjection> findAllByTeamIdOrderByNameAsc(String teamId);

}
