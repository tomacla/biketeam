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

        assertFalse(userRepository.findOne(new SearchUserSpecification(null, null, null, false, null, null, null, "foo@bar.com", null)).isPresent());
        assertFalse(userRepository.findOne(new SearchUserSpecification(null, null, null, false, 21212L, null, null, null, null)).isPresent());

        createUser();

        assertTrue(userRepository.findOne(new SearchUserSpecification(null, null, null, false, null, null, null, "foo@bar.com", null)).isPresent());
        assertTrue(userRepository.findOne(new SearchUserSpecification(null, null, null, false, 21212L, null, null, null, null)).isPresent());


    }

    private User createUser() {
        User user = new User();
        user.setEmail("foo@bar.com");
        user.setStravaId(21212L);
        user.setFirstName("foo");
        user.setLastName("bar");
        userRepository.save(user);
        return user;
    }


}
