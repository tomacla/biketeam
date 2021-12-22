package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.message.RideMessage;
import info.tomacla.biketeam.domain.message.RideMessageRepository;
import info.tomacla.biketeam.domain.message.TripMessage;
import info.tomacla.biketeam.domain.message.TripMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MessageService {

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

    public void save(RideMessage rideMessage) {
        rideMessageRepository.save(rideMessage);
    }

    public void save(TripMessage tripMessage) {
        tripMessageRepository.save(tripMessage);
    }

    public void deleteRideMessage(String id) {
        getRideMessage(id).ifPresent(rideMessage -> rideMessageRepository.delete(rideMessage));
    }

    public void deleteTripMessage(String id) {
        getTripMessage(id).ifPresent(tripMessage -> tripMessageRepository.delete(tripMessage));
    }

    public void deleteByTeam(String teamId) {
        rideMessageRepository.findByTeamId(teamId).forEach(rm -> deleteRideMessage(rm.getId()));
        tripMessageRepository.findByTeamId(teamId).forEach(tm -> deleteTripMessage(tm.getId()));
    }

}
