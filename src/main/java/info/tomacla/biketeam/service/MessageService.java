package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.message.Message;
import info.tomacla.biketeam.domain.message.MessageHolder;
import info.tomacla.biketeam.domain.message.MessageRepository;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.mattermost.MattermostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MattermostService mattermostService;

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
