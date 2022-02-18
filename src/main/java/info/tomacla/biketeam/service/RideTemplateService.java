package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.template.RideTemplate;
import info.tomacla.biketeam.domain.template.RideTemplateProjection;
import info.tomacla.biketeam.domain.template.RideTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RideTemplateService {

    private static final Logger log = LoggerFactory.getLogger(RideTemplateService.class);

    @Autowired
    private RideTemplateRepository rideTemplateRepository;

    public List<RideTemplateProjection> listTemplates(String teamId) {
        return rideTemplateRepository.findAllByTeamIdOrderByNameAsc(teamId);
    }

    public Optional<RideTemplate> get(String teamId, String templateId) {
        final Optional<RideTemplate> optionalRideTemplate = rideTemplateRepository.findById(templateId);
        if (optionalRideTemplate.isPresent() && optionalRideTemplate.get().getTeamId().equals(teamId)) {
            return optionalRideTemplate;
        }
        return Optional.empty();
    }

    @Transactional
    public void save(RideTemplate template) {
        rideTemplateRepository.save(template);
    }

    @Transactional
    public void delete(String teamId, String templateId) {
        log.info("Request ride template deletion {}", templateId);
        get(teamId, templateId).ifPresent(template -> rideTemplateRepository.delete(template));
    }

    @Transactional
    public void increment(String teamId, String templateId) {
        log.info("Request ride template increment {}", templateId);
        get(teamId, templateId).ifPresent(template -> {
            if (template.getIncrement() != null) {
                template.setIncrement(template.getIncrement() + 1);
                save(template);
            }
        });
    }

    public void deleteByTeam(String teamId) {
        rideTemplateRepository.findAllByTeamIdOrderByNameAsc(teamId).stream().map(RideTemplateProjection::getId).forEach(rideTemplateRepository::deleteById);
    }
}
