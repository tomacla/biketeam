package info.tomacla.biketeam.domain.publication;

import info.tomacla.biketeam.common.data.PublishedStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface PublicationRepository extends CrudRepository<Publication, String> {

    List<Publication> findAllByTeamIdAndPublishedStatusAndPublishedAtLessThan(String teamId, PublishedStatus publishedStatus, ZonedDateTime now);

    List<PublicationProjection> findAllByTeamIdOrderByPublishedAtDesc(String teamId);

    Page<Publication> findAllByTeamIdInAndPublishedAtBetweenAndPublishedStatus(Set<String> teamIds, ZonedDateTime from, ZonedDateTime to, PublishedStatus publishedStatus, Pageable pageable);

}
