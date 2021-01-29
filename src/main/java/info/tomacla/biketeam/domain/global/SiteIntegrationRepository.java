package info.tomacla.biketeam.domain.global;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteIntegrationRepository extends CrudRepository<SiteIntegration, Long> {

}
