package info.tomacla.biketeam.service.externalpublication;

import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;

public interface ExternalPublicationService {

    boolean isApplicable(Team team);

    void publish(Team team, Ride ride);

    void publish(Team team, Publication publication);

}
