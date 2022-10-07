package info.tomacla.biketeam.domain.message;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.AbstractDBTest;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.trip.TripRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class TripMessageTest extends AbstractDBTest {

    @Autowired
    private TripMessageRepository tripMessageRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    public void test() {

        final Trip trip = createTrip();
        final User user = createUser();

        TripMessage m = new TripMessage();
        m.setTrip(trip);
        m.setPublishedAt(ZonedDateTime.now().minus(1, ChronoUnit.DAYS));
        m.setContent("test content");
        m.setUser(user);

        assertEquals(0, tripMessageRepository.findAllByTripId(trip.getId()).size());
        assertEquals(0, tripMessageRepository.findAllByUserId(user.getId()).size());

        tripMessageRepository.save(m);

        assertEquals(1, tripMessageRepository.findAllByTripId(trip.getId()).size());
        assertEquals(1, tripMessageRepository.findAllByUserId(user.getId()).size());

    }

    private Trip createTrip() {
        Trip trip = new Trip();
        trip.setTeamId("tripmessagetest-team");
        trip.setTitle("test-ride");
        trip.setStartDate(LocalDate.now());
        trip.setEndDate(LocalDate.now().plus(1, ChronoUnit.DAYS));
        trip.setDescription("description");
        trip.setMeetingTime(LocalTime.MIDNIGHT);
        trip.setPublishedAt(ZonedDateTime.now());
        trip.setPublishedStatus(PublishedStatus.PUBLISHED);
        tripRepository.save(trip);
        return trip;
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
        team.setId("tripmessagetest-team");
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Test");
        teamRepository.save(team);
    }

}
