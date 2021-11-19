package info.tomacla.biketeam.service.broadcast;

import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;

public interface BroadcastService {

    boolean isConfigured(Team team);

    void broadcast(Team team, Ride ride);

    void broadcast(Team team, Publication publication);

    void broadcast(Team team, Trip trip);

}
