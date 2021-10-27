package info.tomacla.biketeam.service.deletion;

import info.tomacla.biketeam.domain.map.MapIdNamePostedAtVisibleProjection;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.domain.publication.PublicationIdTitlePublishedAtProjection;
import info.tomacla.biketeam.domain.publication.PublicationRepository;
import info.tomacla.biketeam.domain.ride.RideIdTitleDateProjection;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.team.TeamRepository;
import info.tomacla.biketeam.domain.trip.TripIdTitleDateProjection;
import info.tomacla.biketeam.domain.trip.TripRepository;
import info.tomacla.biketeam.domain.user.UserRepository;
import info.tomacla.biketeam.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeletionService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MapRepository mapRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileService fileService;

    // TODO should be transactional
    public void deleteTeam(String teamId) {
        teamRepository.findById(teamId).ifPresent(team -> {
            // delete all elements
            rideRepository.findAllByTeamIdOrderByDateDesc(teamId).stream().map(RideIdTitleDateProjection::getId).forEach(rideRepository::deleteById);
            publicationRepository.findAllByTeamIdOrderByPublishedAtDesc(teamId).stream().map(PublicationIdTitlePublishedAtProjection::getId).forEach(publicationRepository::deleteById);
            tripRepository.findAllByTeamIdOrderByStartDateDesc(teamId).stream().map(TripIdTitleDateProjection::getId).forEach(tripRepository::deleteById);
            mapRepository.findAllByTeamIdOrderByPostedAtDesc(teamId).stream().map(MapIdNamePostedAtVisibleProjection::getId).forEach(mapRepository::deleteById);
            fileService.deleteByTeam(teamId);
            // remove all access
            userRepository.findByRoles_Team(team).forEach(user -> {
                user.removeRole(team.getId());
                userRepository.save(user);
            });
            // remove all access to this team
            team.clearRoles();
            teamRepository.save(team);
            // finaly delete the team
            teamRepository.deleteById(teamId);
        });
    }

}
