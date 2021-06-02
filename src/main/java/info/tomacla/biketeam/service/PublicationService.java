package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.ImageDescriptor;
import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.publication.PublicationIdTitlePostedAtProjection;
import info.tomacla.biketeam.domain.publication.PublicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PublicationService {

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private FacebookService facebookService;

    @Autowired
    private FileService fileService;


    public void publishPublications() {
        publicationRepository.findAllByPublishedStatusAndPublishedAtLessThan(PublishedStatus.UNPUBLISHED, ZonedDateTime.now(configurationService.getTimezone())).forEach(pub -> {
            pub.setPublishedStatus(PublishedStatus.PUBLISHED);
            publicationRepository.save(pub);
            facebookService.publish(pub);
        });
    }

    public List<PublicationIdTitlePostedAtProjection> listPublications() {
        return publicationRepository.findAllByOrderByPostedAtDesc();
    }

    public Optional<Publication> get(String publicationId) {
        return publicationRepository.findById(publicationId);
    }

    public void save(Publication publication) {
        publicationRepository.save(publication);
    }

    public void delete(String publicationId) {
        get(publicationId).ifPresent(publication -> publicationRepository.delete(publication));
    }

    public Optional<ImageDescriptor> getImage(String publicationId) {

        Optional<FileExtension> fileExtensionExists = fileService.exists(FileRepositories.PUBLICATION_IMAGES, publicationId, FileExtension.byPriority());

        if (fileExtensionExists.isPresent()) {

            final FileExtension extension = fileExtensionExists.get();
            final Path path = fileService.get(FileRepositories.PUBLICATION_IMAGES, publicationId + extension.getExtension());

            return Optional.of(ImageDescriptor.of(extension, path));

        }

        return Optional.empty();

    }

}
