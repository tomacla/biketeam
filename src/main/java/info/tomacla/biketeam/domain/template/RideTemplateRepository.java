package info.tomacla.biketeam.domain.template;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RideTemplateRepository extends PagingAndSortingRepository<RideTemplate, String>, JpaSpecificationExecutor<RideTemplate> {

}
