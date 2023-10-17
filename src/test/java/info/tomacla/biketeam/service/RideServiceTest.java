package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.ride.SearchRideSpecification;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

class RideServiceTest {

    record TestGroup(int minutes, boolean hasMap) {
    }

    @Test
    void getShortNameTest() {

        RideService rideService = new RideService(null, null, null, null);

        Assertions.assertEquals("RR122", rideService.getShortName(getRide("Raymond Ride #122")));
        Assertions.assertEquals("NP492", rideService.getShortName(getRide("N-Peloton #492")));
        Assertions.assertEquals("MMH12", rideService.getShortName(getRide("MMH #12")));
        Assertions.assertEquals("NBR7", rideService.getShortName(getRide("N-Bappé Ride #7")));
        Assertions.assertEquals("200dR", rideService.getShortName(getRide("200 des Ribines")));
        Assertions.assertEquals("--$*!€", rideService.getShortName(getRide("--$*!€")));
    }

    private Ride getRide(String s) {
        Ride ride = new Ride();
        ride.setTitle(s);
        return ride;
    }

    @Test
    void listMapsForNearestRidesNoRideTest() {
        TeamService teamService = mock(TeamService.class);
        RideRepository rideRepository = mock(RideRepository.class);
        RideService rideService = new RideService(null, rideRepository, teamService, null);

        Team team = new Team();
        TeamConfiguration teamConfiguration = new TeamConfiguration();
        String timezone = "Europe/Paris";
        teamConfiguration.setTimezone(timezone);
        team.setConfiguration(teamConfiguration);
        when(teamService.get("teamId")).thenReturn(Optional.of(team));

        List<Ride> rides = new ArrayList<>();

        SearchRideSpecification spec = new SearchRideSpecification(false, null, null, null, null, Set.of("teamId"), PublishedStatus.PUBLISHED, null, null, LocalDate.now().minus(3, ChronoUnit.DAYS),
                LocalDate.now().plus(7, ChronoUnit.DAYS));

        when(rideRepository.findAll(eq(spec)))
                .thenReturn(rides);

        List<RideGroup> rideGroups = rideService.listRideGroupsByStartProximity("teamId");
        Assertions.assertTrue(rideGroups.isEmpty());
    }

    @Test
    void listMapsForNearestRidesTest() {
        TeamService teamService = mock(TeamService.class);
        RideRepository rideRepository = mock(RideRepository.class);
        RideService rideService = new RideService(null, rideRepository, teamService, null);

        Team team = new Team();
        TeamConfiguration teamConfiguration = new TeamConfiguration();
        String timezone = "Europe/Paris";
        ZoneId zoneId = ZoneId.of(timezone);
        teamConfiguration.setTimezone(timezone);
        team.setConfiguration(teamConfiguration);
        when(teamService.get("teamId")).thenReturn(Optional.of(team));

        AtomicInteger mapIdCounter = new AtomicInteger(1);

        Instant now = Instant.now();

        List<Ride> rides = new ArrayList<>();
        rides.add(getRide(zoneId, now, mapIdCounter,
                new TestGroup(-55, true), // 1 - rank 7
                new TestGroup(-55, true), // 2 - rank 8
                new TestGroup(-40, true), // 3 - rank 5
                new TestGroup(-40, true), // 4 - rank 6
                new TestGroup(-25, true), // 5 - rank 3
                new TestGroup(-25, true), // 6 - rank 4
                new TestGroup(0, false),
                new TestGroup(5, true), // 7 - rank 1
                new TestGroup(5, true) // 8 - rank 2
        ));
        rides.add(getRide(zoneId, now.plus(24, ChronoUnit.HOURS),
                mapIdCounter,
                new TestGroup(0, false),
                new TestGroup(0, true), // 9 - rank 9
                new TestGroup(0, true), // 10 - rank 10
                new TestGroup(0, false),
                new TestGroup(10, true), // 11 - rank 12
                new TestGroup(5, true) // 12 - rank 11
        ));
        rides.add(getRide(zoneId, now.minus(48, ChronoUnit.HOURS),
                mapIdCounter,
                new TestGroup(0, false),
                new TestGroup(-5, true), // 13 - rank 16
                new TestGroup(0, true), // 14 - rank 14
                new TestGroup(0, false),
                new TestGroup(0, true), // 15 - rank 15
                new TestGroup(5, true) // 16 - rank 13
        ));

        SearchRideSpecification spec = new SearchRideSpecification(false, null, null, null, null, Set.of("teamId"), PublishedStatus.PUBLISHED, null, null, LocalDate.now().minus(3, ChronoUnit.DAYS),
                LocalDate.now().plus(7, ChronoUnit.DAYS));

        when(rideRepository.findAll(eq(spec)))
                .thenReturn(rides);

        List<RideGroup> rideGroups = rideService.listRideGroupsByStartProximity("teamId");
        List<String> actualMapIds = rideGroups.stream().map(RideGroup::getMap).map(Map::getId).toList();
        List<String> expectedMapIds = List.of(
                "map07",
                "map08",
                "map05",
                "map06",
                "map03",
                "map04",
                "map01",
                "map02",
                "map09",
                "map10",
                "map12",
                "map11",
                "map16",
                "map14",
                "map15",
                "map13"
        );
        Assertions.assertEquals(expectedMapIds, actualMapIds);
    }

    private Ride getRide(ZoneId zoneId, Instant rideStart, AtomicInteger mapIdCounter, TestGroup... testGroups) {
        Ride ride = new Ride();
        ride.setDate(rideStart.atZone(zoneId).toLocalDate());
        Set<RideGroup> groups = new HashSet<>();

        for (TestGroup testGroup : testGroups) {
            RideGroup rideGroup = new RideGroup();
            rideGroup.setMeetingTime(rideStart.plus(testGroup.minutes(), ChronoUnit.MINUTES).atZone(zoneId).toLocalTime());
            if (testGroup.hasMap()) {
                Map map = createMap(mapIdCounter);
                rideGroup.setMap(map);
            }
            groups.add(rideGroup);
        }

        ride.setGroups(groups);
        ride.setPublishedStatus(PublishedStatus.PUBLISHED);
        return ride;
    }

    private Map createMap(AtomicInteger mapIdCounter) {
        Map map = new Map();
        int id = mapIdCounter.getAndIncrement();
        if (id < 10) {
            map.setId("map0" + id);
        } else {
            map.setId("map" + id);
        }
        return map;
    }

}
