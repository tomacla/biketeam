package info.tomacla.biketeam.domain.ride;

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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class RideTest extends AbstractDBTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private RideRepository rideRepository;

    @Test
    public void rideTest() {

        List<Ride> rides = rideRepository.findAllByDeletionAndTeamIdAndPublishedStatusAndPublishedAtLessThan(false, "ridetest-team",
                PublishedStatus.PUBLISHED,
                ZonedDateTime.now());

        assertEquals(0, rides.size());

        Ride r1 = createRide(ZonedDateTime.now().minus(1, ChronoUnit.DAYS), PublishedStatus.PUBLISHED);
        Ride r2 = createRide(ZonedDateTime.now().minus(1, ChronoUnit.DAYS), PublishedStatus.UNPUBLISHED);
        Ride r3 = createRide(ZonedDateTime.now().plus(1, ChronoUnit.DAYS), PublishedStatus.PUBLISHED);

        rides = rideRepository.findAllByDeletionAndTeamIdAndPublishedStatusAndPublishedAtLessThan(false, "ridetest-team",
                PublishedStatus.PUBLISHED,
                ZonedDateTime.now());

        assertEquals(1, rides.size());

        r1.setDeletion(true);
        rideRepository.save(r1);

        rides = rideRepository.findAllByDeletionAndTeamIdAndPublishedStatusAndPublishedAtLessThan(false, "ridetest-team",
                PublishedStatus.PUBLISHED,
                ZonedDateTime.now());

        assertEquals(0, rides.size());


    }

    private Ride createRide(ZonedDateTime publishedAt, PublishedStatus status) {
        Ride ride = new Ride();
        ride.setTeamId("ridetest-team");
        ride.setTitle("test-ride");
        ride.setDate(LocalDate.now());
        ride.setDescription("description");
        ride.setType(RideType.REGULAR);
        ride.setPublishedAt(publishedAt);
        ride.setPublishedStatus(status);
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

}
