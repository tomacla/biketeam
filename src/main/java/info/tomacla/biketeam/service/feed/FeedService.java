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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
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

    public LocalDate getDefaultFrom(Set<String> teamIds) {
        List<LastTeamData> lastTeamData = teamService.getLastTeamData(teamIds);
        LocalDate defaultIfNull = LocalDate.now().minus(3L, ChronoUnit.MONTHS);
        LocalDate publicationFromDate = LastTeamData.extractDate(lastTeamData, d -> d.getLastPublicationPublishedAt(), defaultIfNull);
        LocalDate rideFromDate = LastTeamData.extractDate(lastTeamData, d -> d.getLastRidePublishedAt(), defaultIfNull);
        LocalDate tripFromDate = LastTeamData.extractDate(lastTeamData, d -> d.getLastTripPublishedAt(), defaultIfNull);
        return Collections.min(Arrays.asList(publicationFromDate, rideFromDate, tripFromDate, defaultIfNull));
    }

    public List<FeedEntity> listFeed(User user, Team team, FeedOptions options) {
        return this.listFeed(user, Set.of(team.getId()), ZoneId.of(team.getConfiguration().getTimezone()), options);
    }

    public List<FeedEntity> listFeed(User user, Set<String> teamIds, ZoneId zoneId, FeedOptions options) {

        Set<FeedEntity> result = new HashSet<>();

        // add publications
        if (!options.isOnlyMyFeed()) {
            result.addAll(publicationService.searchPublications(
                    teamIds,
                    options.getFrom() == null ? null : ZonedDateTime.of(options.getFrom(), LocalTime.MIDNIGHT, ZoneOffset.UTC),
                    options.getTo() == null ? null : ZonedDateTime.of(options.getTo().plus(1, ChronoUnit.DAYS), LocalTime.MIDNIGHT, ZoneOffset.UTC),
                    0,
                    30
            ).getContent());
        }

        // add rides
        if (!options.isOnlyMyFeed()) {
            result.addAll(rideService.searchRides(
                    teamIds,
                    options.getFrom(),
                    options.getTo(),
                    true,
                    0,
                    30
            ).getContent());
        }
        if (user != null) {
            result.addAll(rideService.searchUpcomingRidesByUser(user, teamIds, options.getFrom(), options.getTo()));
        }

        // add trips
        if (!options.isOnlyMyFeed()) {
            result.addAll(tripService.searchTrips(
                    teamIds,
                    options.getFrom(),
                    options.getTo(),
                    true,
                    0,
                    30
            ).getContent());
        }
        if (user != null) {
            result.addAll(tripService.searchUpcomingTripsByUser(user, teamIds, options.getFrom(), options.getTo()));
        }


        return result.stream().sorted(FeedSorter.get(zoneId)).limit(30).collect(Collectors.toList());

    }

}
