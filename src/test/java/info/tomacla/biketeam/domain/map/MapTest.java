package info.tomacla.biketeam.domain.map;

import info.tomacla.biketeam.common.geo.Point;
import info.tomacla.biketeam.domain.AbstractDBTest;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MapTest extends AbstractDBTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MapRepository mapRepository;

    @Test
    public void mapRepositoryTU() {

        Map map = new Map();
        map.setId("mapRepositoryTU-id");
        map.setTeamId("maptest-team");
        map.setPermalink("map-perma");
        map.setLength(100.0);
        map.setPositiveElevation(25.0);
        map.setNegativeElevation(32.0);
        map.setName("map test");
        map.setStartPoint(new Point(52.0, 3.0));
        map.setEndPoint(new Point(53.0, 3.1));

        assertFalse(mapRepository.findById("map-id").isPresent());
        assertFalse(mapRepository.findOne(new SearchMapSpecification(null, "map-perma", null, null, null, null, null, null, null, null, null)).isPresent());

        mapRepository.save(map);

        assertTrue(mapRepository.findById("mapRepositoryTU-id").isPresent());
        assertTrue(mapRepository.findOne(new SearchMapSpecification(null, "map-perma", null, null, null, null, null, null, null, null, null)).isPresent());

        mapRepository.deleteAll();

    }

    @Test
    public void mapRepositoryTU_findall() {

        Map map1 = new Map();
        map1.setId("mapRepositoryTU_findall-id1");
        map1.setTeamId("maptest-team");
        map1.setPermalink("map-perma");
        map1.setLength(100.0);
        map1.setPositiveElevation(25.0);
        map1.setNegativeElevation(32.0);
        map1.setName("road test");
        map1.setStartPoint(new Point(52.0, 3.0));
        map1.setEndPoint(new Point(53.0, 3.1));
        mapRepository.save(map1);

        Map map2 = new Map();
        map2.setId("mapRepositoryTU_findall-id2");
        map2.setTeamId("maptest-team");
        map2.setPermalink("map2-perma");
        map2.setLength(100.0);
        map2.setPositiveElevation(25.0);
        map2.setNegativeElevation(32.0);
        map2.setName("gravel test");
        map2.setStartPoint(new Point(52.0, 3.0));
        map2.setEndPoint(new Point(53.0, 3.1));
        mapRepository.save(map2);

        Map map3 = new Map();
        map3.setId("mapRepositoryTU_findall-id3");
        map3.setTeamId("maptest-team2");
        map3.setPermalink("map3-perma");
        map3.setLength(100.0);
        map3.setPositiveElevation(25.0);
        map3.setNegativeElevation(32.0);
        map3.setName("foobar");
        map3.setStartPoint(new Point(52.0, 3.0));
        map3.setEndPoint(new Point(53.0, 3.1));
        mapRepository.save(map3);


        assertEquals(2, mapRepository.findAll(new SearchMapSpecification(null, null, Set.of("maptest-team"), null, null, null, null, null, null, null, null)).size());
        assertEquals(1, mapRepository.findAll(new SearchMapSpecification(null, null, Set.of("maptest-team"), "GRAVEL", null, null, null, null, null, null, null)).size());

        mapRepository.deleteAll();

    }

    @Test
    public void mapRepositoryTU_tags() {

        Map map1 = new Map();
        map1.setId("mapRepositoryTU_findall-id1");
        map1.setTeamId("maptest-team");
        map1.setPermalink("map-perma");
        map1.setTags(List.of("fondo", "road"));
        map1.setLength(100.0);
        map1.setPositiveElevation(25.0);
        map1.setNegativeElevation(32.0);
        map1.setName("road test");
        map1.setStartPoint(new Point(52.0, 3.0));
        map1.setEndPoint(new Point(53.0, 3.1));
        mapRepository.save(map1);

        Map map2 = new Map();
        map2.setId("mapRepositoryTU_findall-id2");
        map2.setTeamId("maptest-team");
        map2.setPermalink("map2-perma");
        map2.setTags(List.of("fondo", "gravel"));
        map2.setLength(100.0);
        map2.setPositiveElevation(25.0);
        map2.setNegativeElevation(32.0);
        map2.setName("gravel test");
        map2.setStartPoint(new Point(52.0, 3.0));
        map2.setEndPoint(new Point(53.0, 3.1));
        mapRepository.save(map2);

        Map map3 = new Map();
        map3.setId("mapRepositoryTU_findall-id3");
        map3.setTeamId("maptest-team");
        map3.setPermalink("map3-perma");
        map3.setTags(List.of("trip", "bikepack"));
        map3.setLength(100.0);
        map3.setPositiveElevation(25.0);
        map3.setNegativeElevation(32.0);
        map3.setName("foobar");
        map3.setStartPoint(new Point(52.0, 3.0));
        map3.setEndPoint(new Point(53.0, 3.1));
        mapRepository.save(map3);


        assertEquals(Set.of("bikepack", "fondo", "gravel", "road", "trip"), mapRepository.findAllDistinctTags("maptest-team"));
        assertEquals(Set.of("gravel"), mapRepository.findDistinctTagsContaining("maptest-team", "rav"));

        mapRepository.deleteAll();

    }

    @Test
    public void mapRepositoryTU_search() {

        SearchMapSpecification spec = new SearchMapSpecification(
                null,
                null,
                Set.of("maptest-team"),
                null,
                3.0,
                200.0,
                MapType.ROAD,
                0.0,
                1000.0,
                List.of("fondo"),
                WindDirection.NORTH
        );

        Map map1 = new Map();
        map1.setId("mapRepositoryTU_findall-id1");
        map1.setTeamId("maptest-team");
        map1.setPermalink("map-perma");
        map1.setTags(List.of("fondo", "road"));
        map1.setLength(100.0);
        map1.setPositiveElevation(25.0);
        map1.setNegativeElevation(32.0);
        map1.setName("road test");
        map1.setStartPoint(new Point(52.0, 3.0));
        map1.setEndPoint(new Point(53.0, 3.1));
        mapRepository.save(map1);

        Map map2 = new Map();
        map2.setId("mapRepositoryTU_findall-id2");
        map2.setTeamId("maptest-team");
        map2.setPermalink("map2-perma");
        map2.setTags(List.of("fondo", "gravel"));
        map2.setLength(201.0);
        map2.setPositiveElevation(25.0);
        map2.setNegativeElevation(32.0);
        map2.setName("gravel test");
        map2.setStartPoint(new Point(52.0, 3.0));
        map2.setEndPoint(new Point(53.0, 3.1));
        mapRepository.save(map2);

        Map map3 = new Map();
        map3.setId("mapRepositoryTU_findall-id3");
        map3.setTeamId("maptest-team");
        map3.setPermalink("map3-perma");
        map3.setTags(List.of("trip", "bikepack"));
        map3.setLength(100.0);
        map3.setPositiveElevation(25.0);
        map3.setNegativeElevation(32.0);
        map3.setName("foobar");
        map3.setStartPoint(new Point(52.0, 3.0));
        map3.setEndPoint(new Point(53.0, 3.1));
        mapRepository.save(map3);

        List<Map> matches = mapRepository.findAll(spec);
        assertEquals(1, matches.size());
        assertTrue(matches.contains(map1));

        mapRepository.deleteAll();

    }

    @BeforeAll
    public void createTeam() {
        final Team team = new Team();
        team.setId("maptest-team");
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Test");
        teamRepository.save(team);

        final Team team2 = new Team();
        team2.setId("maptest-team2");
        team2.setCity("City");
        team2.getDescription().setDescription("Description");
        team2.setName("Test");
        teamRepository.save(team2);
    }

}
