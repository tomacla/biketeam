package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Exchanges;
import info.tomacla.biketeam.domain.message.Message;
import info.tomacla.biketeam.domain.message.MessageHolder;
import info.tomacla.biketeam.domain.message.MessageRepository;
import info.tomacla.biketeam.domain.message.SearchMessageSpecification;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.service.amqp.BrokerService;
import info.tomacla.biketeam.service.amqp.dto.TeamEntityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private MessageRepository messageRepository;

    public Optional<Message> getMessage(String id) {
        return messageRepository.findById(id);
    }

    public List<Message> listByTarget(MessageHolder holder) {
        return messageRepository.findAll(SearchMessageSpecification.byTargetAndType(holder.getId(), holder.getMessageType()));
    }

    @Transactional
    public void save(MessageHolder holder, Message message) {

        messageRepository.save(message);

        if (holder instanceof Ride) {
            brokerService.sendToBroker(Exchanges.PUBLISH_RIDE_MESSAGE,
                    TeamEntityDTO.valueOf(holder.getTeamId(), message.getId()));
        } else if (holder instanceof Trip) {
            brokerService.sendToBroker(Exchanges.PUBLISH_TRIP_MESSAGE,
                    TeamEntityDTO.valueOf(holder.getTeamId(), message.getId()));
        }

    }

    @Transactional
    public void delete(String id) {
        getMessage(id).ifPresent(message -> {
            messageRepository.deleteReplies(message.getId());
            messageRepository.delete(message);
        });
    }

}