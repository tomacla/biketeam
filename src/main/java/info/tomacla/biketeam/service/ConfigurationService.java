package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.Timezone;
import info.tomacla.biketeam.domain.global.*;
import info.tomacla.biketeam.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationService.class);

    @Autowired
    private SiteDescriptionRepository siteDescriptionRepository;

    @Autowired
    private SiteConfigurationRepository siteConfigurationRepository;

    @Autowired
    private SiteIntegrationRepository siteIntegrationRepository;

    @Autowired
    private UserService userService;

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

    public List<String> getDefaultSearchTags() {
        List<String> defaultSearchTags = getSiteConfiguration().getDefaultSearchTags();
        if (defaultSearchTags == null) {
            return new ArrayList<>();
        }
        return defaultSearchTags;
    }

    public ZoneId getTimezone() {
        return ZoneId.of(getSiteConfiguration().getTimezone());
    }

    public SiteDescription getSiteDescription() {
        return siteDescriptionRepository.findById(1L).get();
    }

    public SiteConfiguration getSiteConfiguration() {
        return siteConfigurationRepository.findById(1L).get();
    }

    public SiteIntegration getSiteIntegration() {
        return siteIntegrationRepository.findById(1L).get();
    }

    public void save(SiteIntegration siteIntegration) {
        log.info("Site integration is updated");
        siteIntegrationRepository.save(siteIntegration);
    }

    public void save(SiteConfiguration siteConfiguration) {
        log.info("Site configuration is updated");
        siteConfigurationRepository.save(siteConfiguration);
    }

    public void save(SiteDescription siteDescription) {
        log.info("Site description is updated");
        siteDescriptionRepository.save(siteDescription);
    }

    @PostConstruct
    public void init() {

        log.info("Initializing application data");

        Optional<SiteDescription> siteDescription = siteDescriptionRepository.findById(1L);
        if (siteDescription.isEmpty()) {
            siteDescriptionRepository.save(new SiteDescription(
                    defaultSiteName,
                    defaultDescription));
        }

        Optional<SiteIntegration> siteIntegration = siteIntegrationRepository.findById(1L);
        if (siteIntegration.isEmpty()) {
            siteIntegrationRepository.save(new SiteIntegration());
        }

        Optional<SiteConfiguration> siteConfiguration = siteConfigurationRepository.findById(1L);
        if (siteConfiguration.isEmpty()) {
            siteConfigurationRepository.save(new SiteConfiguration(Timezone.DEFAULT_TIMEZONE, null));
        }

        if (userService.getByStravaId(adminStravaId).isEmpty()) {
            userService.save(new User(true,
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
