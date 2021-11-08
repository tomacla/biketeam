package info.tomacla.biketeam.service.externalpublication;

import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;

public interface ExternalPublicationService {

    boolean isConfigured(Team team);

    void publish(Team team, Ride ride);

    void publish(Team team, Publication publication);

    void publish(Team team, Trip trip);

}
