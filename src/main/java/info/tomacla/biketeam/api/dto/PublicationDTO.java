package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.publication.Publication;

import java.time.ZonedDateTime;

public class PublicationDTO {

    public String id;
    public String teamId;
    public PublishedStatus publishedStatus;
    public ZonedDateTime publishedAt;
    public String title;
    public String content;
    public boolean imaged;

    public static PublicationDTO valueOf(Publication publication) {

        if (publication == null) {
            return null;
        }

        PublicationDTO dto = new PublicationDTO();
        dto.id = publication.getId();
        dto.teamId = publication.getTeamId();
        dto.publishedStatus = publication.getPublishedStatus();
        dto.publishedAt = publication.getPublishedAt();
        dto.title = publication.getTitle();
        dto.content = publication.getContent();
        dto.imaged = publication.isImaged();

        return dto;
    }

}
