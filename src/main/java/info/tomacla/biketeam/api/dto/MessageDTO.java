package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.domain.message.Message;

import java.time.ZonedDateTime;

public class MessageDTO {

    public String id;
    public MemberDTO author;
    public ZonedDateTime publishedAt;
    public String content;

    public static MessageDTO valueOf(Message message) {

        if (message == null) {
            return null;
        }
        MessageDTO dto = new MessageDTO();
        dto.id = message.getId();
        dto.author = MemberDTO.valueOf(message.getUser());
        dto.publishedAt = message.getPublishedAt();
        dto.content = message.getContent();
        return dto;

    }

}
