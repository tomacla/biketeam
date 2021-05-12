package info.tomacla.biketeam.domain.feed;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
public interface FeedRepository extends PagingAndSortingRepository<Feed, String> {

    Page<Feed> findAllByPublishedAtLessThan(ZonedDateTime now, Pageable pageable);

}
