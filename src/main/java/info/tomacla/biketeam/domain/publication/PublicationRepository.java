package info.tomacla.biketeam.domain.publication;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface PublicationRepository extends CrudRepository<Publication, String> {

    List<Publication> findAllByPublishedAtLessThan(ZonedDateTime now);

    List<PublicationIdTitlePostedAtProjection> findAllByOrderByPostedAtDesc();

}
