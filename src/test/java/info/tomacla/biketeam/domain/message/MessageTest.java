package info.tomacla.biketeam.domain.message;

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
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class MessageTest extends AbstractDBTest {

    @Autowired
    private MessageRepository messageRepository;

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

        Message m = new Message();
        m.setTarget(ride);
        m.setPublishedAt(ZonedDateTime.now().minus(1, ChronoUnit.DAYS));
        m.setContent("test content");
        m.setUser(user);

        assertEquals(0, messageRepository.findAllByTargetIdAndTypeOrderByPublishedAtAsc(ride.getId(), MessageTargetType.RIDE).size());
        assertEquals(0, messageRepository.findAllByUserId(user.getId()).size());

        messageRepository.save(m);

        assertEquals(1, messageRepository.findAllByTargetIdAndTypeOrderByPublishedAtAsc(ride.getId(), MessageTargetType.RIDE).size());
        assertEquals(1, messageRepository.findAllByUserId(user.getId()).size());

    }

    private Ride createRide() {
        Ride ride = new Ride();
        ride.setTeamId("ridemessagetest-team");
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
        u.setEmail("ride@messenger.com");
        userRepository.save(u);
        return u;
    }

    @BeforeAll
    public void createTeam() {
        final Team team = new Team();
        team.setId("ridemessagetest-team");
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Test");
        teamRepository.save(team);
    }

}
