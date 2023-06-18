package info.tomacla.biketeam.domain.notification;

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

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class NotificationTest extends AbstractDBTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;


    @Test
    public void test() {

        final User user = createUser();

        Notification r = new Notification();
        r.setTeamId("notifitest-team");
        r.setCreatedAt(ZonedDateTime.now());
        r.setElementId("11");
        r.setType(NotificationType.NEW_RIDE_MESSAGE);
        r.setUser(user);

        assertEquals(0, notificationRepository.findAllByUserIdAndViewedOrderByCreatedAtDesc(user.getId(), false).size());

        notificationRepository.save(r);

        assertEquals(1, notificationRepository.findAllByUserIdAndViewedOrderByCreatedAtDesc(user.getId(), false).size());

    }

    private User createUser() {
        User u = new User();
        u.setEmail("ride@notification.com");
        userRepository.save(u);
        return u;
    }

    @BeforeAll
    public void createTeam() {
        final Team team = new Team();
        team.setId("notifitest-team");
        team.setCity("City");
        team.getDescription().setDescription("Description");
        team.setName("Test2");
        teamRepository.save(team);
    }

}
