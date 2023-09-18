package info.tomacla.biketeam.domain.publication;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicationRepository extends PagingAndSortingRepository<Publication, String>, JpaSpecificationExecutor<Publication> {


}
