package info.tomacla.biketeam.service;

import info.tomacla.biketeam.domain.template.RideTemplate;
import info.tomacla.biketeam.domain.template.RideTemplateIdNameProjection;
import info.tomacla.biketeam.domain.template.RideTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RideTemplateService {

    @Autowired
    private RideTemplateRepository rideTemplateRepository;

    public List<RideTemplateIdNameProjection> listTemplates() {
        return rideTemplateRepository.findAllByOrderByNameAsc();
    }

    public Optional<RideTemplate> get(String templateId) {
        return rideTemplateRepository.findById(templateId);
    }

    public void save(RideTemplate template) {
        rideTemplateRepository.save(template);
    }

    public void delete(String templateId) {
        get(templateId).ifPresent(template -> rideTemplateRepository.delete(template));
    }
}
