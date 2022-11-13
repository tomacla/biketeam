package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.feed.FeedType;
import info.tomacla.biketeam.domain.publication.Publication;

import java.time.ZonedDateTime;

public class PartialPublicationDTO {

    public FeedType feedType;
    public String id;
    public String teamId;
    public PublishedStatus publishedStatus;
    public ZonedDateTime publishedAt;
    public String title;
    public String content;
    public boolean imaged;

    public static PartialPublicationDTO valueOf(Publication publication) {

        if (publication == null) {
            return null;
        }

        PartialPublicationDTO dto = new PartialPublicationDTO();
        dto.feedType = publication.getFeedType();
        dto.id = publication.getId();
        dto.teamId = publication.getTeamId();
        dto.publishedAt = publication.getPublishedAt();
        dto.publishedStatus = publication.getPublishedStatus();
        dto.title = publication.getTitle();
        dto.content = publication.getContent();
        dto.imaged = publication.isImaged();
        return dto;

    }

}
