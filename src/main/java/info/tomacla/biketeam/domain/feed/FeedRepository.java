package info.tomacla.biketeam.domain.feed;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends PagingAndSortingRepository<Feed, String> {
}
