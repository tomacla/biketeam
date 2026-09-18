package info.tomacla.biketeam.web.publication;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.datatype.Dates;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.publication.PublicationRegistration;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.service.auth.BotProtectionService;
import info.tomacla.biketeam.service.auth.RateLimitService;
import info.tomacla.biketeam.service.file.ThumbnailService;
import info.tomacla.biketeam.service.mail.MailSenderService;
import info.tomacla.biketeam.service.url.UrlService;
import info.tomacla.biketeam.web.AbstractController;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Set;

@Controller
@RequestMapping(value = "/{teamId}/publications")
public class PublicationController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(PublicationController.class);

    private static final String REGISTRATION_ANSWER = "Inscription enregistrée - Merci de confirmer votre adresse email";

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private ThumbnailService thumbnailService;

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private BotProtectionService botProtectionService;

    @Autowired
    private RateLimitService rateLimitService;

    @Value("${publication.register.max-per-hour:10}")
    private int maxRegisterPerHour;


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
                                                      @PathVariable("publicationId") String publicationId) {
        final Optional<ImageDescriptor> image = publicationService.getImage(teamId, publicationId);
        if (image.isPresent()) {
            try {

                final ImageDescriptor targetImage = image.get();
                final FileExtension targetImageExtension = targetImage.getExtension();

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", targetImageExtension.getMediaType());
                headers.add("Cache-Control", "public; max-age=604800");
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(publicationId + targetImageExtension.getExtension())
                        .build());

                byte[] bytes = Files.readAllBytes(targetImage.getPath());

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
                                      @RequestParam(value = "altcha", required = false) String altcha,
                                      @RequestParam(value = BotProtectionService.HONEYPOT_FIELD, required = false) String honeypot,
                                      @RequestParam(value = "formStamp", required = false) String formStamp,
                                      HttpServletRequest request,
                                      RedirectAttributes attributes,
                                      Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        try {
            Optional<Publication> optionalPublication = publicationService.get(team.getId(), publicationId);
            if (optionalPublication.isEmpty()) {
                return viewHandler.redirectView(team, "/");
            }

            Publication publication = optionalPublication.get();

            // le formulaire n'est affiche que dans ce cas : un envoi hors de ce cadre vient d'un robot
            if (!publication.isAllowRegistration()
                    || !PublishedStatus.PUBLISHED.equals(publication.getPublishedStatus())) {
                return viewHandler.redirectView(team, "/publications/" + publicationId);
            }

            final BotProtectionService.Verdict verdict = botProtectionService.check(altcha, honeypot, formStamp);
            if (verdict == BotProtectionService.Verdict.HONEYPOT) {
                // robot : meme reponse qu'une inscription reussie, sans aucun envoi
                log.info("Publication registration rejected by bot protection: {}", verdict);
                attributes.addFlashAttribute("infos", List.of(REGISTRATION_ANSWER));
                return viewHandler.redirectView(team, "/publications/" + publicationId);
            }
            if (verdict != BotProtectionService.Verdict.HUMAN) {
                log.info("Publication registration rejected by bot protection: {}", verdict);
                attributes.addFlashAttribute("error", BotProtectionService.errorMessage(verdict));
                return viewHandler.redirectView(team, "/publications/" + publicationId);
            }

            if (!rateLimitService.tryAcquire(rateLimitService.clientKey(request, "publication-register"), maxRegisterPerHour)) {
                attributes.addFlashAttribute("error", "Trop de demandes depuis cette connexion. Réessayez dans quelques minutes.");
                return viewHandler.redirectView(team, "/publications/" + publicationId);
            }

            PublicationRegistration registration = new PublicationRegistration();
            registration.setUserEmail(email);
            registration.setUserName(firstname + " " + lastname);
            registration.setPublication(publication);
            registration.setUserEmailValid(false);

            this.sendConfirmationEmail(registration);

            publication.getRegistrations().add(registration);

            publicationService.save(publication);

            attributes.addFlashAttribute("infos", List.of(REGISTRATION_ANSWER));
            return viewHandler.redirectView(team, "/publications/" + publicationId);

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/publications/" + publicationId);
        }
    }

    private void sendConfirmationEmail(PublicationRegistration registration) {

        String url = urlService.getUrlWithSuffix("/confirm-email?code=" + registration.getUserEmailCode());

        StringBuilder sb = new StringBuilder();
        sb.append("<html>").append("<head></head>").append("<body>");
        sb.append("<h5>").append("Confirmation de votre adresse email").append("</h5>");
        sb.append("<p>").append("Suite à votre inscription sur un événement, merci de bien vouloir confirmer votre adresse email en cliquant sur le lien ci dessous.").append("</p>");
        sb.append("<p>").append(getHtmlLink(url)).append("</p>");
        sb.append("</body>").append("</html>");

        mailSenderService.sendDirectly(null, Set.of(registration.getUserEmail()), "Confirmation requise", sb.toString(), null);

    }

    private String getHtmlLink(String href) {
        return "<a href=\"" + href + "\">" + href + "</a>";
    }

}
