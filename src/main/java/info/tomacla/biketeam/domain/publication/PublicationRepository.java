package info.tomacla.biketeam.domain.publication;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicationRepository extends CrudRepository<Publication, String> {

    List<Publication> findAll();

    List<PublicationIdTitlePostedAtProjection> findAllByOrderByPostedAtDesc();

}
