package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamConfiguration;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.gpx.GpxService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

class MapServiceTest {

    record TestGroup(int minutes, boolean hasMap) {
    }

    @Test
    void listMapsForNearestRidesNoRideTest() {
        GpxService gpxService = mock(GpxService.class);
        FileService fileService = mock(FileService.class);
        RideService rideService = mock(RideService.class);
        TeamService teamService = mock(TeamService.class);
        MapRepository mapRepository = mock(MapRepository.class);
        MapService mapService = new MapService(gpxService, fileService, rideService, teamService, mapRepository);

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
        rides.add(getRide(zoneId, PublishedStatus.UNPUBLISHED, now, mapIdCounter,
                new TestGroup(0, true),
                new TestGroup(0, true),
                new TestGroup(0, true),
                new TestGroup(0, true)
        ));

        when(rideService.searchRides(eq(Set.of("teamId")), eq(0), eq(10), any(), any()))
                .thenReturn(new PageImpl<>(rides));

        List<Map> findMapsResult = new ArrayList<>();
        findMapsResult.add(createMap(mapIdCounter));
        findMapsResult.add(createMap(mapIdCounter));
        findMapsResult.add(createMap(mapIdCounter));
        findMapsResult.add(createMap(mapIdCounter));
        when(mapRepository.findByTeamId(eq("teamId"), any())).thenReturn(new PageImpl<>(findMapsResult));

        List<Map> maps = mapService.listMapsForNearestRides("teamId");
        Assertions.assertEquals(findMapsResult, maps);
    }

    @Test
    void listMapsForNearestRidesTest() {
        GpxService gpxService = mock(GpxService.class);
        FileService fileService = mock(FileService.class);
        RideService rideService = mock(RideService.class);
        TeamService teamService = mock(TeamService.class);
        MapRepository mapRepository = mock(MapRepository.class);
        MapService mapService = new MapService(gpxService, fileService, rideService, teamService, mapRepository);

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
        rides.add(getRide(zoneId, PublishedStatus.UNPUBLISHED, now, mapIdCounter,
                new TestGroup(0, true), // 1 - not included
                new TestGroup(0, true), // 2 - not included
                new TestGroup(0, true), // 3 - not included
                new TestGroup(0, true) // 4 - not included
        ));
        rides.add(getRide(zoneId, PublishedStatus.PUBLISHED, now, mapIdCounter,
                new TestGroup(-55, true), // 5 - rank 7
                new TestGroup(-55, true), // 6 - rank 8
                new TestGroup(-40, true), // 7 - rank 5
                new TestGroup(-40, true), // 8 - rank 6
                new TestGroup(-25, true), // 9 - rank 3
                new TestGroup(-25, true), // 10 - rank 4
                new TestGroup(0, false),
                new TestGroup(5, true), // 11 - rank 1
                new TestGroup(5, true) // 12 - rank 2
        ));
        rides.add(getRide(zoneId, PublishedStatus.PUBLISHED, now.plus(24, ChronoUnit.HOURS),
                mapIdCounter,
                new TestGroup(0, false),
                new TestGroup(0, true), // 13 - rank 9
                new TestGroup(0, true), // 14 - rank 10
                new TestGroup(0, false),
                new TestGroup(10, true), // 15 - rank 12
                new TestGroup(5, true) // 16 - rank 11
        ));
        rides.add(getRide(zoneId, PublishedStatus.PUBLISHED, now.minus(48, ChronoUnit.HOURS),
                mapIdCounter,
                new TestGroup(0, false),
                new TestGroup(-5, true), // 17 - rank 16
                new TestGroup(0, true), // 18 - rank 14
                new TestGroup(0, false),
                new TestGroup(0, true), // 19 - rank 15
                new TestGroup(5, true) // 20 - rank 13
        ));

        when(rideService.searchRides(eq(Set.of("teamId")), eq(0), eq(10), any(), any()))
                .thenReturn(new PageImpl<>(rides));

        List<Map> maps = mapService.listMapsForNearestRides("teamId");
        List<String> actualMapIds = maps.stream().map(Map::getId).toList();
        List<String> expectedMapIds = List.of(
                "map11",
                "map12",
                "map09",
                "map10",
                "map07",
                "map08",
                "map05",
                "map06",
                "map13",
                "map14",
                "map16",
                "map15",
                "map20",
                "map18",
                "map19",
                "map17"
        );
        Assertions.assertEquals(expectedMapIds, actualMapIds);
    }

    private Ride getRide(ZoneId zoneId, PublishedStatus publishedStatus, Instant rideStart, AtomicInteger mapIdCounter, TestGroup... testGroups) {
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
        ride.setPublishedStatus(publishedStatus);
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
