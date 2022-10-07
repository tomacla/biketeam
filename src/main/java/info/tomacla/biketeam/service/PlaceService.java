package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.place.*;
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

    public void save(Place place) {
        placeRepository.save(place);
    }

    public Optional<Place> get(String teamId, String placeId) {
        Optional<Place> optionalPlace = placeRepository.findById(placeId);
        if (optionalPlace.isPresent() && optionalPlace.get().getTeamId().equals(teamId)) {
            return optionalPlace;
        }
        return Optional.empty();
    }

    public void delete(String teamId, String placeId) {
        get(teamId, placeId).ifPresent(this::delete);
    }

    @Transactional
    public void delete(Place place) {
        log.info("Request place deletion {}", place.getId());
        placeRepository.removeStartPlaceIdInRide(place.getId());
        placeRepository.removeEndPlaceIdInRide(place.getId());
        placeRepository.removeStartPlaceIdInTrip(place.getId());
        placeRepository.removeEndPlaceIdInTrip(place.getId());
        placeRepository.removeStartPlaceIdInRideTemplate(place.getId());
        placeRepository.removeEndPlaceIdInRideTemplate(place.getId());
        placeRepository.delete(place);
        log.info("Place deleted {}", place.getId());
    }

    public List<Place> listPlaces(String teamId) {
        return placeRepository.findAllByTeamIdOrderByNameAsc(teamId);
    }

    public List<PlaceAppearanceProjection> listPlacesWithAppearances(String teamId, PlaceSorterOption option) {
        List<PlaceAppearanceProjection> result = placeRepository.findAllByTeamIdWithAppearances(teamId);
        result.sort(PlaceSorter.of(option));
        return result;
    }

    public void deleteByTeam(String teamId) {
        placeRepository.findAllByTeamIdOrderByNameAsc(teamId).stream().map(Place::getId).forEach(placeRepository::deleteById);
    }

}
