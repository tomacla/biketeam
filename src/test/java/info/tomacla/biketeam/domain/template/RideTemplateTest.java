package info.tomacla.biketeam.domain.template;

import info.tomacla.biketeam.domain.AbstractDBTest;
import info.tomacla.biketeam.domain.ride.RideType;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class RideTemplateTest extends AbstractDBTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private RideTemplateRepository rideTemplateRepository;

    @Test
    public void rideTest() {

        List<RideTemplateProjection> templates = rideTemplateRepository.findAllByTeamIdOrderByNameAsc("ridetmptest-team");

        assertEquals(0, templates.size());

        createRidetemplate();

        templates = rideTemplateRepository.findAllByTeamIdOrderByNameAsc("ridetmptest-team");

        assertEquals(1, templates.size());


    }

    private void createRidetemplate() {
        RideTemplate template = new RideTemplate();
        template.setTeamId("ridetmptest-team");
        template.setName("test-ridetemplate");
        template.setIncrement(8);
        template.setDescription("description");
        template.setType(RideType.REGULAR);
        rideTemplateRepository.save(template);
    }

    @BeforeAll
    public void createTeam() {
        final Team team = new Team();
        team.setId("ridetmptest-team");
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Test");
        teamRepository.save(team);
    }


}
