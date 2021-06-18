package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.feed.Feed;
import info.tomacla.biketeam.domain.feed.FeedRepository;
import info.tomacla.biketeam.domain.team.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class FeedService {

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private TeamService teamService;

    public List<Feed> listFeed(String teamId) {
        Team team = teamService.get(teamId).orElseThrow(() -> new IllegalArgumentException("Unknown team ID"));
        return feedRepository.findAllByTeamIdAndPublishedAtLessThan(
                teamId,
                ZonedDateTime.now(ZoneId.of(team.getConfiguration().getTimezone())),
                PageRequest.of(0, 15, Sort.by("publishedAt").descending())).getContent();
    }

}
