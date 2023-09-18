package info.tomacla.biketeam.domain.team;

import info.tomacla.biketeam.domain.AbstractDBTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class TeamTest extends AbstractDBTest {

    @Autowired
    private TeamRepository teamRepository;


    @Test
    public void teamTest() {

        List<Team> teams = teamRepository.findAll(SearchTeamSpecification.all());
        List<Team> teamsPublic = teamRepository.findAll(SearchTeamSpecification.publiclyVisible());

        int before = teams.size();
        int beforePublic = teamsPublic.size();

        createTeam("team1-test", Visibility.PUBLIC);
        createTeam("team2-test", Visibility.PRIVATE);
        createTeam("team3-test", Visibility.PUBLIC_UNLISTED);


        teams = teamRepository.findAll(SearchTeamSpecification.all());
        teamsPublic = teamRepository.findAll(SearchTeamSpecification.publiclyVisible());

        assertEquals(before + 3, teams.size());
        assertEquals(beforePublic + 1, teamsPublic.size());


    }

    public void createTeam(String teamId, Visibility visibility) {
        final Team team = new Team();
        team.setId(teamId);
        team.setVisibility(visibility);
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Test");
        teamRepository.save(team);
    }


}
