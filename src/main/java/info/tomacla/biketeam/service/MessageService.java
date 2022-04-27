package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.message.RideMessage;
import info.tomacla.biketeam.domain.message.RideMessageRepository;
import info.tomacla.biketeam.domain.message.TripMessage;
import info.tomacla.biketeam.domain.message.TripMessageRepository;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.mattermost.MattermostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MattermostService mattermostService;

    @Autowired
    private RideMessageRepository rideMessageRepository;

    @Autowired
    private TripMessageRepository tripMessageRepository;

    public Optional<RideMessage> getRideMessage(String id) {
        return rideMessageRepository.findById(id);
    }

    public Optional<TripMessage> getTripMessage(String id) {
        return tripMessageRepository.findById(id);
    }

    public void save(Team team, RideMessage rideMessage) {
        rideMessageRepository.save(rideMessage);
        mattermostService.notify(team, rideMessage);
    }

    public void save(Team team, TripMessage tripMessage) {
        tripMessageRepository.save(tripMessage);
        mattermostService.notify(team, tripMessage);
    }

    public void deleteRideMessage(String id) {
        getRideMessage(id).ifPresent(rideMessage -> rideMessageRepository.delete(rideMessage));
    }

    public void deleteTripMessage(String id) {
        getTripMessage(id).ifPresent(tripMessage -> tripMessageRepository.delete(tripMessage));
    }

    public void deleteByUser(String userId) {
        rideMessageRepository.deleteByUserId(userId);
        tripMessageRepository.deleteByUserId(userId);
    }
}
