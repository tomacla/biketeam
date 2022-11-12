package info.tomacla.biketeam.domain.reaction;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.AbstractDBTest;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.ride.RideType;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ReactionTest extends AbstractDBTest {

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    public void test() {

        final Ride ride = createRide();
        final User user = createUser();

        Reaction r = new Reaction();
        r.setTarget(ride);
        r.setContent("\uD83D\uDC95");
        r.setUser(user);

        assertEquals(0, reactionRepository.findAllByTargetIdAndType(ride.getId(), ReactionTargetType.RIDE).size());
        assertEquals(0, reactionRepository.findAllByUserId(user.getId()).size());

        reactionRepository.save(r);

        assertEquals(1, reactionRepository.findAllByTargetIdAndType(ride.getId(), ReactionTargetType.RIDE).size());
        assertEquals(1, reactionRepository.findAllByUserId(user.getId()).size());

    }

    private Ride createRide() {
        Ride ride = new Ride();
        ride.setTeamId("reactiontest-team");
        ride.setTitle("test-ride");
        ride.setDate(LocalDate.now());
        ride.setDescription("description");
        ride.setType(RideType.REGULAR);
        ride.setPublishedAt(ZonedDateTime.now());
        ride.setPublishedStatus(PublishedStatus.PUBLISHED);
        rideRepository.save(ride);
        return ride;
    }

    private User createUser() {
        User u = new User();
        u.setEmail("ride@reaction.com");
        userRepository.save(u);
        return u;
    }

    @BeforeAll
    public void createTeam() {
        final Team team = new Team();
        team.setId("reactiontest-team");
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Test");
        teamRepository.save(team);
    }

}
