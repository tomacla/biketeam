package info.tomacla.biketeam.domain.place;

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class PlaceTest extends AbstractDBTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Test
    public void placeRepositoryTU() {

        Place place = new Place();
        place.setId("placeRepositoryTU-id");
        place.setTeamId("placetest-team");
        place.setPoint(new Point(52.0, 3.0));
        place.setLink("http://toto");
        place.setName("place 1");
        place.setAddress("somewhere");

        assertFalse(placeRepository.findById("placeRepositoryTU-id").isPresent());

        placeRepository.save(place);

        assertTrue(placeRepository.findById("placeRepositoryTU-id").isPresent());

        placeRepository.deleteAll();

    }

    @BeforeAll
    public void createTeam() {
        final Team team = new Team();
        team.setId("placetest-team");
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Test");
        teamRepository.save(team);

        final Team team2 = new Team();
        team2.setId("placetest-team2");
        team2.setCity("City");
        team2.getDescription().setDescription("Description");
        team2.setName("Test");
        teamRepository.save(team2);
    }

}
