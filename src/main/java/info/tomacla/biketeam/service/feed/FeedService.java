package info.tomacla.biketeam.service.feed;

import info.tomacla.biketeam.domain.feed.FeedEntity;
import info.tomacla.biketeam.domain.feed.FeedOptions;
import info.tomacla.biketeam.domain.feed.FeedSorter;
import info.tomacla.biketeam.domain.team.LastTeamData;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.TripService;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeedService {

    @Autowired
    private PublicationService publicationService;
    @Autowired
    private RideService rideService;
    @Autowired
    private TripService tripService;
    @Autowired
    private TeamService teamService;

    public List<FeedEntity> listFeed(User user, Team team, FeedOptions options) {
        return this.listFeed(user, Set.of(team.getId()), ZoneId.of(team.getConfiguration().getTimezone()), options);
    }

    public List<FeedEntity> listFeed(User user, Set<String> teamIds, ZoneId zoneId, FeedOptions options) {

        Set<FeedEntity> result = new HashSet<>();

        List<LastTeamData> lastTeamData = teamService.getLastTeamData(teamIds);

        LocalDate publicationFromDate = options.getFrom() != null ? options.getFrom() : LastTeamData.extractDate(lastTeamData, d -> d.getLastPublicationPublishedAt(), LocalDate.now().minus(1L, ChronoUnit.MONTHS));
        LocalDate rideFromDate = options.getFrom() != null ? options.getFrom() : LastTeamData.extractDate(lastTeamData, d -> d.getLastRidePublishedAt(), LocalDate.now().minus(1L, ChronoUnit.MONTHS));
        LocalDate tripFromDate = options.getFrom() != null ? options.getFrom() : LastTeamData.extractDate(lastTeamData, d -> d.getLastTripPublishedAt(), LocalDate.now().minus(1L, ChronoUnit.MONTHS));

        LocalDate publicationToDate = options.getTo();
        LocalDate rideToDate = options.getTo();
        LocalDate tripToDate = options.getTo();

        // add publications
        if(!options.isOnlyMyFeed()) {
            result.addAll(publicationService.searchPublications(
                    teamIds,
                    ZonedDateTime.of(publicationFromDate, LocalTime.MIDNIGHT, ZoneOffset.UTC),
                    publicationToDate == null ? null : ZonedDateTime.of(publicationToDate.plus(1, ChronoUnit.DAYS), LocalTime.MIDNIGHT, ZoneOffset.UTC),
                    0,
                    30
            ).getContent());
        }

        // add rides
        if(!options.isOnlyMyFeed()) {
            result.addAll(rideService.searchRides(
                    teamIds,
                    rideFromDate,
                    rideToDate,
                    true,
                    0,
                    30
            ).getContent());
        }
        if (user != null) {
            result.addAll(rideService.searchUpcomingRidesByUser(user, teamIds, rideFromDate, rideToDate));
        }

        // add trips
        if(!options.isOnlyMyFeed()) {
            result.addAll(tripService.searchTrips(
                    teamIds,
                    tripFromDate,
                    tripToDate,
                    true,
                    0,
                    30
            ).getContent());
        }
        if (user != null) {
            result.addAll(tripService.searchUpcomingTripsByUser(user, teamIds, tripFromDate, tripToDate));
        }


        return result.stream().sorted(FeedSorter.get(zoneId)).limit(30).collect(Collectors.toList());

    }

}
