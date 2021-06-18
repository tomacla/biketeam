package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.ImageDescriptor;
import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.publication.PublicationIdTitlePostedAtProjection;
import info.tomacla.biketeam.domain.publication.PublicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PublicationService {

    private static final Logger log = LoggerFactory.getLogger(PublicationService.class);

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private TeamService teamService;

    @Autowired
    private FacebookService facebookService;

    @Autowired
    private FileService fileService;

    @Autowired
    private MailService mailService;

    public void publishPublications() {
        teamService.list().forEach(team ->
            publicationRepository.findAllByTeamIdAndPublishedStatusAndPublishedAtLessThan(
                    team.getId(),
                    PublishedStatus.UNPUBLISHED,
                    ZonedDateTime.now(ZoneId.of(team.getConfiguration().getTimezone()))
            ).forEach(pub -> {
                log.info("Publishing publication {}", pub.getId());
                pub.setPublishedStatus(PublishedStatus.PUBLISHED);
                publicationRepository.save(pub);
                facebookService.publish(team, pub);
                mailService.publish(team, pub);
            })
        );
    }

    public List<PublicationIdTitlePostedAtProjection> listPublications(String teamId) {
        return publicationRepository.findAllByTeamIdOrderByPostedAtDesc(teamId);
    }

    public Optional<Publication> get(String teamId, String publicationId) {
        final Optional<Publication> optionalPublication = publicationRepository.findById(publicationId);
        if (optionalPublication.isPresent() && optionalPublication.get().getTeamId().equals(teamId)) {
            return optionalPublication;
        }
        return Optional.empty();
    }

    public void save(Publication publication) {
        publicationRepository.save(publication);
    }

    public void delete(String teamId, String publicationId) {
        log.info("Request publication deletion {}", publicationId);
        final Optional<Publication> optionalPublication = get(teamId, publicationId);
        if (optionalPublication.isPresent()) {
            final Publication publication = optionalPublication.get();
            getImage(publication.getTeamId(), publication.getId()).ifPresent(image ->
                    fileService.delete(FileRepositories.PUBLICATION_IMAGES, publication.getTeamId(), publication.getId() + image.getExtension().getExtension())
            );
            publicationRepository.delete(publication);
        }
    }

    public Optional<ImageDescriptor> getImage(String teamId, String publicationId) {

        Optional<FileExtension> fileExtensionExists = fileService.exists(FileRepositories.PUBLICATION_IMAGES, teamId, publicationId, FileExtension.byPriority());

        if (fileExtensionExists.isPresent()) {

            final FileExtension extension = fileExtensionExists.get();
            final Path path = fileService.get(FileRepositories.PUBLICATION_IMAGES, teamId, publicationId + extension.getExtension());

            return Optional.of(ImageDescriptor.of(extension, path));

        }

        return Optional.empty();

    }

    public void saveImage(String teamId, String publicationId, InputStream is, String fileName) {
        Optional<FileExtension> optionalFileExtension = FileExtension.findByFileName(fileName);
        if (optionalFileExtension.isPresent()) {
            Path newImage = fileService.getTempFileFromInputStream(is);
            fileService.store(newImage, FileRepositories.PUBLICATION_IMAGES, teamId, publicationId + optionalFileExtension.get().getExtension());
        }
    }

}
