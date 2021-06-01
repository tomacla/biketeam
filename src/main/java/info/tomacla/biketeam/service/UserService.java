package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Optional<User> getByStravaId(Long stravaId) {
        return userRepository.findByStravaId(stravaId);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public Optional<User> get(String userId) {
        return userRepository.findById(userId);
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }
}
