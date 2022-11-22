package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.message.Message;
import info.tomacla.biketeam.domain.message.MessageHolder;
import info.tomacla.biketeam.domain.message.MessageRepository;
import info.tomacla.biketeam.domain.message.MessageTargetType;
import info.tomacla.biketeam.domain.notification.Notification;
import info.tomacla.biketeam.domain.notification.NotificationType;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.service.mattermost.MattermostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MattermostService mattermostService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MessageRepository messageRepository;

    public List<Message> listByTarget(MessageHolder holder) {
        return messageRepository.findAllByTargetIdAndTypeOrderByPublishedAtAsc(holder.getId(), holder.getMessageType());
    }

    public Optional<Message> getMessage(String id) {
        return messageRepository.findById(id);
    }

    public void save(Team team, MessageHolder holder, Message message) {

        messageRepository.save(message);
        mattermostService.notify(team, message, holder);

        final Message replyTo = message.getReplyToId() != null ? getMessage(message.getReplyToId()).orElse(null) : null;

        team.getRoles().stream()
                .filter(r -> ((replyTo != null && replyTo.getUser().equals(r.getUser())) || r.getRole().equals(Role.ADMIN)) && !r.getUser().getId().equals(message.getUser().getId()))
                .forEach(role -> {

                    Notification n = new Notification();
                    n.setTeamId(team.getId());
                    n.setElementId(holder.getId());
                    n.setUser(role.getUser());
                    if (holder.getMessageType().equals(MessageTargetType.TRIP)) {
                        n.setType(NotificationType.NEW_TRIP_MESSAGE);
                    } else {
                        n.setType(NotificationType.NEW_RIDE_MESSAGE);
                    }
                    n.setViewed(false);
                    n.setCreatedAt(ZonedDateTime.now(ZoneOffset.UTC));
                    notificationService.save(n);

                });

    }

    public void delete(String id) {
        getMessage(id).ifPresent(message -> {
            messageRepository.deleteReplies(id);
            messageRepository.delete(message);
        });
    }

    public void deleteByUser(String userId) {
        messageRepository.deleteByUserId(userId);
    }

    public void deleteByTarget(String targetId) {
        messageRepository.deleteByTargetId(targetId);
    }

}
