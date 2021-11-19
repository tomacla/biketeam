package info.tomacla.biketeam.web.team.publication;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/admin/publications")
public class AdminTeamPublicationController extends AbstractController {

    @Autowired
    private PublicationService publicationService;

    @GetMapping
    public String getPublications(@PathVariable("teamId") String teamId,
                                  @ModelAttribute("error") String error,
                                  Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "Administration - Publications", team);
        model.addAttribute("publications", publicationService.listPublications(team.getId()));
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_publications";
    }

    @GetMapping(value = "/{publicationId}")
    public String editPublication(@PathVariable("teamId") String teamId,
                                  @PathVariable("publicationId") String publicationId,
                                  @ModelAttribute("error") String error,
                                  Principal principal,
                                  Model model) {

        final Team team = checkTeam(teamId);

        boolean published = false;

        NewPublicationForm.NewPublicationFormBuilder builder = NewPublicationForm.builder(ZonedDateTime.now(), team.getZoneId());

        if (!publicationId.equals("new")) {

            Optional<Publication> optionalPublication = publicationService.get(team.getId(), publicationId);
            if (optionalPublication.isEmpty()) {
                return viewHandler.redirect(team, "/admin/publications");
            }

            Publication publication = optionalPublication.get();
            builder.withContent(publication.getContent())
                    .withId(publication.getId())
                    .withTitle(publication.getTitle())
                    .withPublishedAt(publication.getPublishedAt(), team.getZoneId());

            published = publication.getPublishedStatus().equals(PublishedStatus.PUBLISHED);
        }

        NewPublicationForm form = builder.get();

        addGlobalValues(principal, model, "Administration - Modifier la publication", team);
        model.addAttribute("formdata", form);
        model.addAttribute("published", published);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_publications_new";
    }

    @PostMapping(value = "/{publicationId}")
    public RedirectView editPublication(@PathVariable("teamId") String teamId,
                                        @PathVariable("publicationId") String publicationId,
                                        RedirectAttributes attributes,
                                        Principal principal, Model model,
                                        NewPublicationForm form) {

        final Team team = checkTeam(teamId);
        final ZoneId timezone = team.getZoneId();

        try {

            NewPublicationForm.NewPublicationFormParser parser = form.parser();
            Publication publication;
            if (!publicationId.equals("new")) {

                Optional<Publication> optionalPublication = publicationService.get(team.getId(), publicationId);
                if (optionalPublication.isEmpty()) {
                    return viewHandler.redirectView(team, "/admin/publications");
                }

                publication = optionalPublication.get();
                publication.setContent(parser.getContent());
                publication.setTitle(parser.getTitle());
                publication.setPublishedAt(parser.getPublishedAt(timezone));

            } else {
                publication = new Publication(team.getId(),
                        parser.getTitle(),
                        parser.getContent(),
                        parser.getPublishedAt(timezone),
                        parser.getFile().isPresent()
                );
            }

            if (parser.getFile().isPresent()) {
                publication.setImaged(true);
                MultipartFile uploadedFile = parser.getFile().get();
                publicationService.saveImage(team.getId(), publication.getId(), form.getFile().getInputStream(), uploadedFile.getOriginalFilename());
            }

            publicationService.save(publication);

            return viewHandler.redirectView(team, "/admin/publications");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/publications/" + publicationId);
        }

    }

    @GetMapping(value = "/delete/{publicationId}")
    public RedirectView deletePublication(@PathVariable("teamId") String teamId,
                                          @PathVariable("publicationId") String publicationId,
                                          RedirectAttributes attributes,
                                          Principal principal,
                                          Model model) {

        final Team team = checkTeam(teamId);

        try {
            publicationService.delete(team.getId(), publicationId);
            return viewHandler.redirectView(team, "/admin/publications");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/publications");
        }

    }


}
