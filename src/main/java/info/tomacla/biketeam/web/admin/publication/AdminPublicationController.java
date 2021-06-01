package info.tomacla.biketeam.web.admin.publication;

import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.service.FileService;
import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/admin/publications")
public class AdminPublicationController extends AbstractController {

    @Autowired
    private FileService fileService;

    @Autowired
    private PublicationService publicationService;

    @GetMapping
    public String getPublications(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Publications");
        model.addAttribute("publications", publicationService.listPublications());
        return "admin_publications";
    }

    @PostMapping(value = "/{publicationId}")
    public String editPublication(@PathVariable("publicationId") String publicationId,
                                  Principal principal,
                                  Model model,
                                  NewPublicationForm form) {

        NewPublicationForm.NewPublicationFormParser parser = form.parser();

        try {

            Publication publication;
            if (!publicationId.equals("new")) {

                Optional<Publication> optionalPublication = publicationService.get(publicationId);
                if (optionalPublication.isEmpty()) {
                    return "redirect:/admin/publications";
                }

                publication = optionalPublication.get();
                publication.setContent(parser.getContent());
                publication.setTitle(parser.getTitle());
                publication.setPublishedAt(parser.getPublishedAt(configurationService.getTimezone()));

                if (parser.getFile().isPresent()) {
                    publication.setImaged(true);
                }

            } else {
                publication = new Publication(parser.getTitle(),
                        parser.getContent(),
                        parser.getPublishedAt(configurationService.getTimezone()),
                        parser.getFile().isPresent()
                );
            }

            if (parser.getFile().isPresent()) {
                MultipartFile uploadedFile = parser.getFile().get();
                Optional<FileExtension> optionalFileExtension = FileExtension.findByFileName(uploadedFile.getOriginalFilename());
                if (optionalFileExtension.isPresent()) {
                    Path newImage = fileService.getTempFileFromInputStream(form.getFile().getInputStream());
                    fileService.store(newImage, FileRepositories.PUBLICATION_IMAGES, publication.getId() + optionalFileExtension.get().getExtension());
                }
            }

            publicationService.save(publication);

            return "redirect:/admin/publications";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Modifier la publication");
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "admin_publications_new";
        }

    }

    @GetMapping(value = "/{publicationId}")
    public String editPublication(@PathVariable("publicationId") String publicationId,
                                  Principal principal,
                                  Model model) {

        boolean published = false;

        NewPublicationForm.NewPublicationFormBuilder builder = NewPublicationForm.builder();
        if (!publicationId.equals("new")) {

            Optional<Publication> optionalPublication = publicationService.get(publicationId);
            if (optionalPublication.isEmpty()) {
                return "redirect:/admin/publications";
            }

            Publication publication = optionalPublication.get();
            builder.withContent(publication.getContent())
                    .withId(publication.getId())
                    .withTitle(publication.getTitle())
                    .withPublishedAt(publication.getPublishedAt());

            published = publication.getPublishedStatus().equals(PublishedStatus.PUBLISHED);
        }

        NewPublicationForm form = builder.get();

        addGlobalValues(principal, model, "Administration - Modifier la publication");
        model.addAttribute("formdata", form);
        model.addAttribute("published", published);
        return "admin_publications_new";
    }

    @GetMapping(value = "/delete/{publicationId}")
    public String deletePublication(@PathVariable("publicationId") String publicationId,
                                    Model model) {

        try {
            publicationService.delete(publicationId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/publications";
    }

}
