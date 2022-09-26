package info.tomacla.biketeam.domain.feed;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.AbstractDBTest;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideRepository;
import info.tomacla.biketeam.domain.ride.RideType;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class FeedTest extends AbstractDBTest {

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private RideRepository rideRepository;


    @Test
    public void feedRepositoryTU() {

        Page<Feed> feed = feedRepository.findAllByTeamIdInAndPublishedAtLessThan(Set.of("feedtest-team"),
                ZonedDateTime.now(),
                Pageable.unpaged());

        assertEquals(0, feed.getSize());

        createRide(ZonedDateTime.now().minus(1, ChronoUnit.DAYS), PublishedStatus.PUBLISHED);
        createRide(ZonedDateTime.now().minus(1, ChronoUnit.DAYS), PublishedStatus.UNPUBLISHED);
        createRide(ZonedDateTime.now().plus(1, ChronoUnit.DAYS), PublishedStatus.PUBLISHED);

        feed = feedRepository.findAllByTeamIdInAndPublishedAtLessThan(Set.of("feedtest-team"),
                ZonedDateTime.now(),
                Pageable.unpaged());

        assertEquals(1, feed.getSize());


    }

    @NotNull
    private void createRide(ZonedDateTime publishedAt, PublishedStatus status) {
        Ride ride = new Ride();
        ride.setTeamId("feedtest-team");
        ride.setTitle("test-ride");
        ride.setDate(LocalDate.now());
        ride.setDescription("description");
        ride.setType(RideType.REGULAR);
        ride.setPublishedAt(publishedAt);
        ride.setPublishedStatus(status);
        rideRepository.save(ride);
    }

    @BeforeAll
    public void createTeam() {
        final Team team = new Team();
        team.setId("feedtest-team");
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Test");
        teamRepository.save(team);
    }

}
