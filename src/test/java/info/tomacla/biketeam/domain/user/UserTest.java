package info.tomacla.biketeam.domain.user;

import info.tomacla.biketeam.domain.AbstractDBTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class UserTest extends AbstractDBTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void userTest() {

        assertFalse(userRepository.findByEmail("foo@bar.com").isPresent());
        assertFalse(userRepository.findByStravaId(21212L).isPresent());

        createUser();

        assertTrue(userRepository.findByEmail("foo@bar.com").isPresent());
        assertTrue(userRepository.findByStravaId(21212L).isPresent());


    }

    private User createUser() {
        User user = new User();
        user.setEmail("foo@bar.com");
        user.setStravaId(21212L);
        user.setFirstName("foo");
        user.setLastName("bar");
        user.setId("user-id");
        userRepository.save(user);
        return user;
    }


}
