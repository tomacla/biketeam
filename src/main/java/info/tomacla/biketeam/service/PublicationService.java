package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Exchanges;
import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.publication.PublicationRepository;
import info.tomacla.biketeam.domain.publication.SearchPublicationSpecification;
import info.tomacla.biketeam.service.amqp.BrokerService;
import info.tomacla.biketeam.service.amqp.dto.TeamEntityDTO;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.image.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class PublicationService {

    private static final Logger log = LoggerFactory.getLogger(PublicationService.class);

    private PublicationRepository publicationRepository;

    private TeamService teamService;

    private BrokerService brokerService;

    private final ImageService imageService;

    public PublicationService(PublicationRepository publicationRepository, TeamService teamService, FileService fileService, BrokerService brokerService) {
        this.publicationRepository = publicationRepository;
        this.teamService = teamService;
        this.brokerService = brokerService;
        this.imageService = new ImageService(FileRepositories.PUBLICATION_IMAGES, fileService);
    }

    @Transactional
    public void save(Publication publication) {
        publicationRepository.save(publication);
    }

    public Page<Publication> listPublications(String teamId, String title, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("publishedAt").descending());
        return publicationRepository.findAll(SearchPublicationSpecification.byTitleInTeam(teamId, title), pageable);
    }

    public Optional<Publication> get(String teamId, String publicationId) {
        final Optional<Publication> optionalPublication = publicationRepository.findById(publicationId);
        if (optionalPublication.isPresent() && optionalPublication.get().getTeamId().equals(teamId)) {
            return optionalPublication;
        }
        return Optional.empty();
    }

    public Page<Publication> searchPublications(Set<String> teamIds, ZonedDateTime from, ZonedDateTime to, int page, int pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("publishedAt").descending());

        SearchPublicationSpecification spec = new SearchPublicationSpecification(
                false,
                teamIds,
                null,
                PublishedStatus.PUBLISHED,
                from,
                to
        );

        return publicationRepository.findAll(spec, pageable);
    }

    @Transactional
    public void delete(String teamId, String publicationId) {
        log.info("Request publication deletion {}", publicationId);
        get(teamId, publicationId).ifPresent(publication -> {
            publication.setDeletion(true);
            save(publication);
        });
    }


    public void deleteImage(String teamId, String publicationId) {
        imageService.delete(teamId, publicationId);
    }

    public Optional<ImageDescriptor> getImage(String teamId, String publicationId) {
        return imageService.get(teamId, publicationId);
    }

    public void saveImage(String teamId, String publicationId, InputStream is, String fileName) {
        imageService.save(teamId, publicationId, is, fileName);
    }

    @RabbitListener(queues = Queues.TASK_PUBLISH_PUBLICATIONS)
    public void publishPublications() {
        teamService.list().forEach(team -> {

                    SearchPublicationSpecification spec = new SearchPublicationSpecification(
                            false,
                            Set.of(team.getId()),
                            null,
                            PublishedStatus.UNPUBLISHED,
                            null,
                            ZonedDateTime.now(team.getZoneId())
                    );

                    publicationRepository.findAll(spec).forEach(pub -> {
                        log.info("Publishing publication {}", pub.getId());
                        pub.setPublishedStatus(PublishedStatus.PUBLISHED);
                        publicationRepository.save(pub);
                        brokerService.sendToBroker(Exchanges.PUBLISH_PUBLICATION,
                                TeamEntityDTO.valueOf(pub.getTeamId(), pub.getId()));
                    });
                }
        );
    }

}
