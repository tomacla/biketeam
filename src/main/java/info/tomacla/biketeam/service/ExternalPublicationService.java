package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;

public interface ExternalPublicationService {

    void publish(Ride ride);

    void publish(Publication publication);

}
