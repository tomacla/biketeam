package info.tomacla.biketeam.service.feed;

import info.tomacla.biketeam.domain.feed.FeedEntity;
import info.tomacla.biketeam.domain.feed.FeedOptions;
import info.tomacla.biketeam.domain.feed.FeedSorter;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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

    public List<FeedEntity> listFeed(User user, Team team, FeedOptions options) {
        return this.listFeed(user, Set.of(team.getId()), ZoneId.of(team.getConfiguration().getTimezone()), options);
    }

    public List<FeedEntity> listFeed(User user, Set<String> teamIds, ZoneId zoneId, FeedOptions options) {

        Set<FeedEntity> result = new HashSet<>();

        // TODO parallel requests to database
        if (options.isIncludePublications()) {
            result.addAll(publicationService.searchPublications(teamIds, ZonedDateTime.of(options.getFrom(), LocalTime.MIDNIGHT, zoneId),
                    ZonedDateTime.of(options.getTo(), LocalTime.MIDNIGHT, zoneId), 0, 10

            ).getContent());
        }
        if (options.isIncludeRides()) {
            result.addAll(rideService.searchRides(teamIds, options.getFrom(), options.getTo(), true, 0, 10).getContent());
            if (user != null) {
                result.addAll(rideService.searchUpcomingRidesByUser(user, teamIds, LocalDate.now().minus(1, ChronoUnit.DAYS)));
            }
        }
        if (options.isIncludeTrips()) {
            result.addAll(tripService.searchTrips(teamIds, options.getFrom(), options.getTo(), true, 0, 10).getContent());
            if (user != null) {
                result.addAll(tripService.searchUpcomingTripsByUser(user, teamIds, LocalDate.now().minus(1, ChronoUnit.DAYS)));
            }
        }

        return result.stream().sorted(FeedSorter.get(zoneId)).collect(Collectors.toList());

    }

}
