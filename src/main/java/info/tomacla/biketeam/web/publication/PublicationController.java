package info.tomacla.biketeam.web.publication;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.publication.PublicationRegistration;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.service.file.ThumbnailService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/publications")
public class PublicationController extends AbstractController {

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private ThumbnailService thumbnailService;


    @GetMapping(value = "/{publicationId}")
    public String getPublication(@PathVariable("teamId") String teamId,
                                 @PathVariable("publicationId") String publicationId,
                                 @ModelAttribute("error") String error,
                                 Principal principal,
                                 Model model) {

        final Team team = checkTeam(teamId);

        Optional<Publication> optionalPublication = publicationService.get(team.getId(), publicationId);
        if (optionalPublication.isEmpty()) {
            return viewHandler.redirect(team, "/");
        }

        Publication publication = optionalPublication.get();

        if (!publication.getPublishedStatus().equals(PublishedStatus.PUBLISHED) && !isAdmin(principal, team)) {
            return viewHandler.redirect(team, "/");
        }

        addGlobalValues(principal, model, "Publication " + publication.getTitle(), team);
        model.addAttribute("publication", publication);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "publication";
    }

    @ResponseBody
    @RequestMapping(value = "/{publicationId}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getPublicationImage(@PathVariable("teamId") String teamId,
                                                      @PathVariable("publicationId") String publicationId,
                                                      @RequestParam(name = "width", defaultValue = "-1", required = false) int targetWidth) {
        final Optional<ImageDescriptor> image = publicationService.getImage(teamId, publicationId);
        if (image.isPresent()) {
            try {

                final ImageDescriptor targetImage = image.get();
                final FileExtension targetImageExtension = targetImage.getExtension();

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", targetImageExtension.getMediaType());
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(publicationId + targetImageExtension.getExtension())
                        .build());

                byte[] bytes = Files.readAllBytes(targetImage.getPath());
                if (targetWidth != -1) {
                    bytes = thumbnailService.resizeImage(bytes, targetWidth, targetImageExtension);
                }

                return new ResponseEntity<>(
                        bytes,
                        headers,
                        HttpStatus.OK
                );

            } catch (IOException e) {
                throw new ServerErrorException("Error while reading publication image : " + publicationId, e);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find publication image : " + publicationId);
    }

    @PostMapping(value = "/{publicationId}/register")
    public RedirectView registerToPub(@PathVariable("teamId") String teamId,
                                      @PathVariable("publicationId") String publicationId,
                                      @RequestParam("firstname") String firstname,
                                      @RequestParam("lastname") String lastname,
                                      @RequestParam("email") String email,
                                      RedirectAttributes attributes,
                                      Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        try {
            Optional<Publication> optionalPublication = publicationService.get(team.getId(), publicationId);
            if (optionalPublication.isEmpty()) {
                return viewHandler.redirectView(team, "/");
            }

            Publication publication = optionalPublication.get();

            PublicationRegistration registration = new PublicationRegistration();
            registration.setUserEmail(email);
            registration.setUserName(firstname + " " + lastname);
            registration.setPublication(publication);

            publication.getRegistrations().add(registration);

            publicationService.save(publication);

            attributes.addFlashAttribute("infos", List.of("Inscription enregistrée"));
            return viewHandler.redirectView(team, "/publications/" + publicationId);

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/publications/" + publicationId);
        }
    }

}
