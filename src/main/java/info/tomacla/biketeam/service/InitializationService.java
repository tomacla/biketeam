package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.global.GlobalData;
import info.tomacla.biketeam.domain.global.GlobalDataRepository;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
public class InitializationService {

    @Autowired
    private GlobalDataRepository globalDataRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${default.sitename}")
    private String defaultSiteName;

    @Value("${default.description}")
    private String defaultDescription;

    @Value("${admin.strava-id}")
    private Long adminStravaId;

    @Value("${admin.first-name}")
    private String adminFirstName;

    @Value("${admin.last-name}")
    private String adminLastName;

    @PostConstruct
    public void init() {

        Optional<GlobalData> globalData = globalDataRepository.findById(1L);
        if (globalData.isEmpty()) {
            globalDataRepository.save(new GlobalData(
                    defaultSiteName,
                    defaultDescription));
        }

        if (userRepository.findByStravaId(adminStravaId).isEmpty()) {
            userRepository.save(new User(true,
                    adminFirstName,
                    adminLastName,
                    adminStravaId,
                    null,
                    null,
                    null,
                    null));
        }

    }


}
