package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.publication.PublicationProjection;
import info.tomacla.biketeam.domain.publication.PublicationRepository;
import info.tomacla.biketeam.service.broadcast.Broadcaster;
import info.tomacla.biketeam.service.file.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.file.Path;
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
    private FileService fileService;

    @Autowired
    private Broadcaster broadcaster;

    @Transactional
    public void save(Publication publication) {
        publicationRepository.save(publication);
    }

    public List<PublicationProjection> listPublications(String teamId) {
        return publicationRepository.findAllByTeamIdOrderByPublishedAtDesc(teamId);
    }

    public Optional<Publication> get(String teamId, String publicationId) {
        final Optional<Publication> optionalPublication = publicationRepository.findById(publicationId);
        if (optionalPublication.isPresent() && optionalPublication.get().getTeamId().equals(teamId)) {
            return optionalPublication;
        }
        return Optional.empty();
    }

    @Transactional
    public void delete(String teamId, String publicationId) {
        log.info("Request publication deletion {}", publicationId);
        final Optional<Publication> optionalPublication = get(teamId, publicationId);
        if (optionalPublication.isPresent()) {
            final Publication publication = optionalPublication.get();
            deleteImage(publication.getTeamId(), publication.getId());
            publicationRepository.delete(publication);
        }
    }

    public void deleteImage(String teamId, String publicationId) {
        getImage(teamId, publicationId).ifPresent(image ->
                fileService.deleteFile(FileRepositories.PUBLICATION_IMAGES, teamId, publicationId + image.getExtension().getExtension())
        );
    }

    public Optional<ImageDescriptor> getImage(String teamId, String publicationId) {

        Optional<FileExtension> fileExtensionExists = fileService.fileExists(FileRepositories.PUBLICATION_IMAGES, teamId, publicationId, FileExtension.byPriority());

        if (fileExtensionExists.isPresent()) {

            final FileExtension extension = fileExtensionExists.get();
            final Path path = fileService.getFile(FileRepositories.PUBLICATION_IMAGES, teamId, publicationId + extension.getExtension());

            return Optional.of(ImageDescriptor.of(extension, path));

        }

        return Optional.empty();

    }

    public void saveImage(String teamId, String publicationId, InputStream is, String fileName) {
        Optional<FileExtension> optionalFileExtension = FileExtension.findByFileName(fileName);
        if (optionalFileExtension.isPresent()) {
            deleteImage(teamId, publicationId);
            Path newImage = fileService.getTempFileFromInputStream(is);
            fileService.storeFile(newImage, FileRepositories.PUBLICATION_IMAGES, teamId, publicationId + optionalFileExtension.get().getExtension());
        }
    }

    public void publishPublications() {
        teamService.list().forEach(team ->
                publicationRepository.findAllByTeamIdAndPublishedStatusAndPublishedAtLessThan(
                        team.getId(),
                        PublishedStatus.UNPUBLISHED,
                        ZonedDateTime.now(team.getZoneId())
                ).forEach(pub -> {
                    log.info("Publishing publication {}", pub.getId());
                    pub.setPublishedStatus(PublishedStatus.PUBLISHED);
                    publicationRepository.save(pub);
                    broadcaster.broadcast(team, pub);
                })
        );
    }

    public void deleteByTeam(String teamId) {
        publicationRepository.findAllByTeamIdOrderByPublishedAtDesc(teamId).stream().map(PublicationProjection::getId).forEach(publicationRepository::deleteById);
    }
}
