package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Exchanges;
import info.tomacla.biketeam.domain.message.Message;
import info.tomacla.biketeam.domain.message.MessageHolder;
import info.tomacla.biketeam.domain.message.MessageRepository;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.service.amqp.BrokerService;
import info.tomacla.biketeam.service.amqp.dto.TeamEntityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private BrokerService brokerService;

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

        if (holder instanceof Ride) {
            brokerService.sendToBroker(Exchanges.PUBLISH_RIDE_MESSAGE,
                    TeamEntityDTO.valueOf(holder.getTeamId(), holder.getId()));
        } else if (holder instanceof Trip) {
            brokerService.sendToBroker(Exchanges.PUBLISH_TRIP_MESSAGE,
                    TeamEntityDTO.valueOf(holder.getTeamId(), holder.getId()));
        }

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