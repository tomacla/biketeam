package info.tomacla.biketeam.domain.ride;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.AbstractDBTest;
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
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class RideTest extends AbstractDBTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void rideTest() {

        List<Ride> rides = rideRepository.findAllByDeletionAndTeamIdAndPublishedStatusAndPublishedAtLessThan(false,
                "ridetest-team",
                PublishedStatus.PUBLISHED,
                ZonedDateTime.now());

        assertEquals(0, rides.size());

        User u1 = createUser(2343L);
        User u2 = createUser(2345L);

        Ride r1 = createRide(ZonedDateTime.now().minus(1, ChronoUnit.DAYS), PublishedStatus.PUBLISHED, Set.of(u1));
        Ride r2 = createRide(ZonedDateTime.now().minus(1, ChronoUnit.DAYS), PublishedStatus.UNPUBLISHED, Set.of(u2));
        Ride r3 = createRide(ZonedDateTime.now().plus(1, ChronoUnit.DAYS), PublishedStatus.PUBLISHED, new HashSet<>());
        Ride r4 = createRide(ZonedDateTime.now().minus(1, ChronoUnit.DAYS), PublishedStatus.PUBLISHED, Set.of(u1, u2));
        Ride r5 = createRide(ZonedDateTime.now().minus(1, ChronoUnit.DAYS), PublishedStatus.PUBLISHED, Set.of(u1));

        rides = rideRepository.findAllByDeletionAndTeamIdAndPublishedStatusAndPublishedAtLessThan(false,
                "ridetest-team",
                PublishedStatus.PUBLISHED,
                ZonedDateTime.now());

        assertEquals(3, rides.size());
        assertEquals(3, rideRepository.findAllByDeletionAndTeamIdInAndGroups_Participants_IdAndDateGreaterThanAndPublishedStatus(
                false,  Set.of("ridetest-team"),""+2343L, ZonedDateTime.now().minus(1, ChronoUnit.DAYS).toLocalDate(), PublishedStatus.PUBLISHED
        ).size());
        assertEquals(1, rideRepository.findAllByDeletionAndTeamIdInAndGroups_Participants_IdAndDateGreaterThanAndPublishedStatus(
                false,  Set.of("ridetest-team"),""+2345L, ZonedDateTime.now().minus(1, ChronoUnit.DAYS).toLocalDate(), PublishedStatus.PUBLISHED
        ).size());

        r1.setDeletion(true);
        rideRepository.save(r1);

        rides = rideRepository.findAllByDeletionAndTeamIdAndPublishedStatusAndPublishedAtLessThan(false,
                "ridetest-team",
                PublishedStatus.PUBLISHED,
                ZonedDateTime.now());

        assertEquals(2, rides.size());


    }

    private Ride createRide(ZonedDateTime publishedAt, PublishedStatus status, Set<User> participants) {
        Ride ride = new Ride();
        ride.setTeamId("ridetest-team");
        ride.setTitle("test-ride");
        ride.setListedInFeed(true);
        ride.setDate(LocalDate.now());
        ride.setDescription("description");
        ride.setType(RideType.REGULAR);
        ride.setPublishedAt(publishedAt);
        ride.setPublishedStatus(status);

        RideGroup rr = new RideGroup();
        rr.setName("G1");
        rr.setRide(ride);
        rr.setMeetingTime(LocalTime.MIDNIGHT);
        rr.setParticipants(participants);
        ride.addGroup(rr);

        rideRepository.save(ride);
        return ride;
    }

    @BeforeAll
    public void createTeam() {
        final Team team = new Team();
        team.setId("ridetest-team");
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Test");
        teamRepository.save(team);
    }

    private User createUser(Long stravaId) {
        User user = new User();
        user.setEmail("foo"+stravaId+"@bar.com");
        user.setStravaId(stravaId);
        user.setFirstName("foo");
        user.setLastName("bar");
        user.setId(""+stravaId);
        userRepository.save(user);
        return user;
    }

}
