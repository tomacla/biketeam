package info.tomacla.biketeam.domain.trip;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.AbstractDBTest;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class TripTest extends AbstractDBTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TripRepository tripRepository;

    @Test
    public void tripTest() {

        List<Trip> trips = tripRepository.findAllByDeletionAndTeamIdAndPublishedStatusAndPublishedAtLessThan(false, "triptest-team",
                PublishedStatus.PUBLISHED,
                ZonedDateTime.now());

        assertEquals(0, trips.size());

        createTrip(ZonedDateTime.now().minus(1, ChronoUnit.DAYS), PublishedStatus.PUBLISHED);
        createTrip(ZonedDateTime.now().minus(1, ChronoUnit.DAYS), PublishedStatus.UNPUBLISHED);
        createTrip(ZonedDateTime.now().plus(1, ChronoUnit.DAYS), PublishedStatus.PUBLISHED);

        trips = tripRepository.findAllByDeletionAndTeamIdAndPublishedStatusAndPublishedAtLessThan(false, "triptest-team",
                PublishedStatus.PUBLISHED,
                ZonedDateTime.now());

        assertEquals(1, trips.size());


    }

    private void createTrip(ZonedDateTime publishedAt, PublishedStatus status) {
        Trip trip = new Trip();
        trip.setTeamId("triptest-team");
        trip.setTitle("test-trip");
        trip.setStartDate(LocalDate.now());
        trip.setEndDate(LocalDate.now().plus(1, ChronoUnit.DAYS));
        trip.setDescription("description");
        trip.setPublishedAt(publishedAt);
        trip.setPublishedStatus(status);
        trip.setMeetingTime(LocalTime.MIDNIGHT);
        tripRepository.save(trip);
    }

    @BeforeAll
    public void createTeam() {
        final Team team = new Team();
        team.setId("triptest-team");
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Test");
        teamRepository.save(team);
    }

}
