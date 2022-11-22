package info.tomacla.biketeam.web.publication;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.reaction.Reaction;
import info.tomacla.biketeam.domain.reaction.ReactionContent;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.service.ReactionService;
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

import javax.servlet.http.HttpSession;
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

    @Autowired
    private ReactionService reactionService;

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

    @GetMapping(value = "/{publicationId}/reactions")
    public String getReactionFragment(@PathVariable("teamId") String teamId,
                                      @PathVariable("publicationId") String publicationId,
                                      @ModelAttribute("error") String error,
                                      HttpSession session,
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

        addGlobalValues(principal, model, "Publication " + publication.getTitle(), team, session);
        model.addAttribute("urlPartPrefix", "publications");
        model.addAttribute("element", publication);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "_fragment_reactions";
    }

    @GetMapping(value = "/{publicationId}/add-reaction/{content}")
    public String addReaction(@PathVariable("teamId") String teamId,
                              @PathVariable("publicationId") String publicationId,
                              @PathVariable("content") String content,
                              @ModelAttribute("error") String error,
                              HttpSession session,
                              Model model,
                              Principal principal) {

        final Team team = checkTeam(teamId);

        Optional<Publication> optionalPublication = publicationService.get(team.getId(), publicationId);
        if (optionalPublication.isEmpty()) {
            return viewHandler.redirect(team, "/");
        }

        Publication publication = optionalPublication.get();
        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isEmpty()) {
            return viewHandler.redirect(team, "/");
        }

        User connectedUser = optionalConnectedUser.get();
        ReactionContent parsedContent = ReactionContent.valueOfUnicode(content);
        Reaction reaction = new Reaction();
        reaction.setTarget(publication);
        reaction.setContent(parsedContent.unicode());
        reaction.setUser(connectedUser);

        publication.getReactions().add(reaction);
        reactionService.save(reaction);

        addGlobalValues(principal, model, "Publication " + publication.getTitle(), team, session);
        model.addAttribute("urlPartPrefix", "publications");
        model.addAttribute("element", publication);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }

        return "_fragment_reactions";

    }

    @GetMapping(value = "/{publicationId}/remove-reaction")
    public String removeReaction(@PathVariable("teamId") String teamId,
                                 @PathVariable("publicationId") String publicationId,
                                 @ModelAttribute("error") String error,
                                 HttpSession session,
                                 Model model,
                                 Principal principal) {

        final Team team = checkTeam(teamId);


        Optional<Publication> optionalPublication = publicationService.get(team.getId(), publicationId);
        if (optionalPublication.isEmpty()) {
            return viewHandler.redirect(team, "/");
        }

        Publication publication = optionalPublication.get();

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isPresent()) {

            User connectedUser = optionalConnectedUser.get();
            final Optional<Reaction> optionalReaction = reactionService.getReaction(publicationId, connectedUser.getId());

            Reaction reaction = optionalReaction.get();
            if (optionalReaction.isPresent()) {
                reactionService.delete(reaction.getId());
                publication.getReactions().remove(reaction);
            }

        }


        addGlobalValues(principal, model, "Publication " + publication.getTitle(), team, session);
        model.addAttribute("urlPartPrefix", "publications");
        model.addAttribute("element", publication);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }

        return "_fragment_reactions";

    }

}
