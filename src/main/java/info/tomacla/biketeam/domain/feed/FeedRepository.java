package info.tomacla.biketeam.domain.feed;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Set;

@Repository
public interface FeedRepository extends PagingAndSortingRepository<Feed, String> {

    Page<Feed> findAllByTeamIdAndPublishedAtLessThan(String teamId, ZonedDateTime now, Pageable pageable);

    Page<Feed> findAllByTeamIdInAndPublishedAtLessThan(Set<String> teamIds, ZonedDateTime now, Pageable pageable);

}
