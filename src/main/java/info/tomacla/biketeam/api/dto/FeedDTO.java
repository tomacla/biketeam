package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.domain.feed.Feed;
import info.tomacla.biketeam.domain.feed.FeedType;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public class FeedDTO {

    public String id;
    public String permalink;
    public String teamId;
    public String teamName;
    public FeedType type;
    public ZonedDateTime publishedAt;
    public String title;
    public LocalDate date;
    public String content;
    public List<String> badges;
    public boolean imaged;

    public static FeedDTO valueOf(Feed feed) {

        if(feed == null) {
            return null;
        }

        FeedDTO dto = new FeedDTO();
        dto.id = feed.getId();
        dto.permalink = feed.getPermalink();
        dto.teamId = feed.getTeamId();
        dto.teamName = feed.getTeamName();
        dto.type = feed.getType();
        dto.publishedAt = feed.getPublishedAt();
        dto.title = feed.getTitle();
        dto.date = feed.getDate();
        dto.content = feed.getContent();
        dto.badges = feed.getBadges();
        dto.imaged = feed.isImaged();
        return dto;

    }

}
