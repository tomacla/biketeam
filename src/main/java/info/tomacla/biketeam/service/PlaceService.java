package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.place.Place;
import info.tomacla.biketeam.domain.place.PlaceRepository;
import info.tomacla.biketeam.domain.place.SearchPlaceSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PlaceService {

    private static final Logger log = LoggerFactory.getLogger(MapService.class);

    @Autowired
    private PlaceRepository placeRepository;

    public Optional<Place> get(String teamId, String placeId) {
        Optional<Place> optionalPlace = placeRepository.findById(placeId);
        if (optionalPlace.isPresent() && optionalPlace.get().getTeamId().equals(teamId)) {
            return optionalPlace;
        }
        return Optional.empty();
    }

    public List<Place> listPlaces(String teamId) {
        SearchPlaceSpecification spec = new SearchPlaceSpecification(teamId);
        return placeRepository.findAll(spec);
    }

    @Transactional
    public void save(Place place) {
        placeRepository.save(place);
    }

    @Transactional
    public void delete(String teamId, String placeId) {
        log.info("Request place deletion {} in team {}", placeId, teamId);
        get(teamId, placeId).ifPresent(place -> {
            placeRepository.removeStartPlaceIdInRide(place.getId());
            placeRepository.removeEndPlaceIdInRide(place.getId());
            placeRepository.removeStartPlaceIdInTrip(place.getId());
            placeRepository.removeEndPlaceIdInTrip(place.getId());
            placeRepository.removeStartPlaceIdInRideTemplate(place.getId());
            placeRepository.removeEndPlaceIdInRideTemplate(place.getId());
            placeRepository.delete(place);
        });
        log.info("Place deleted {}", placeId);
    }


}
