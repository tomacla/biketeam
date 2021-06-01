package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.feed.Feed;
import info.tomacla.biketeam.domain.feed.FeedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class FeedService {

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private ConfigurationService configurationService;

    public List<Feed> listFeed() {
        return feedRepository.findAllByPublishedAtLessThan(
                ZonedDateTime.now(configurationService.getTimezone()),
                PageRequest.of(0, 15, Sort.by("publishedAt").descending())).getContent();
    }

}
