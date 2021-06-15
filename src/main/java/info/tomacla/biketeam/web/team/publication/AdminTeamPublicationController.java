package info.tomacla.biketeam.web.team.publication;

import info.tomacla.biketeam.common.PublishedStatus;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.team.Team;
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

import java.security.Principal;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/admin/publications")
public class AdminTeamPublicationController extends AbstractController {

    @Autowired
    private FileService fileService;

    @Autowired
    private PublicationService publicationService;

    @GetMapping
    public String getPublications(@PathVariable("teamId") String teamId,
                                  Principal principal, Model model) {

        checkAdmin(principal, teamId);
        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "Administration - Publications", team);
        model.addAttribute("publications", publicationService.listPublications(teamId));
        return "team_admin_publications";
    }

    @PostMapping(value = "/{publicationId}")
    public String editPublication(@PathVariable("teamId") String teamId,
                                  @PathVariable("publicationId") String publicationId,
                                  Principal principal,
                                  Model model,
                                  NewPublicationForm form) {

        checkAdmin(principal, teamId);
        final Team team = checkTeam(teamId);
        final ZoneId timezone = ZoneId.of(team.getConfiguration().getTimezone());

        NewPublicationForm.NewPublicationFormParser parser = form.parser();

        try {

            Publication publication;
            if (!publicationId.equals("new")) {

                Optional<Publication> optionalPublication = publicationService.get(teamId, publicationId);
                if (optionalPublication.isEmpty()) {
                    return redirectToAdminPublications(teamId);
                }

                publication = optionalPublication.get();
                publication.setContent(parser.getContent());
                publication.setTitle(parser.getTitle());
                publication.setPublishedAt(parser.getPublishedAt(timezone));

            } else {
                publication = new Publication(teamId,
                        parser.getTitle(),
                        parser.getContent(),
                        parser.getPublishedAt(timezone),
                        parser.getFile().isPresent()
                );
            }

            if (parser.getFile().isPresent()) {
                publication.setImaged(true);
                MultipartFile uploadedFile = parser.getFile().get();
                publicationService.saveImage(teamId, publication.getId(), form.getFile().getInputStream(), uploadedFile.getOriginalFilename());
            }

            publicationService.save(publication);

            return redirectToAdminPublications(teamId);

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Modifier la publication", team);
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "team_admin_publications_new";
        }

    }

    @GetMapping(value = "/{publicationId}")
    public String editPublication(@PathVariable("teamId") String teamId,
                                  @PathVariable("publicationId") String publicationId,
                                  Principal principal,
                                  Model model) {

        checkAdmin(principal, teamId);
        final Team team = checkTeam(teamId);

        boolean published = false;

        NewPublicationForm.NewPublicationFormBuilder builder = NewPublicationForm.builder();
        if (!publicationId.equals("new")) {

            Optional<Publication> optionalPublication = publicationService.get(teamId, publicationId);
            if (optionalPublication.isEmpty()) {
                return redirectToAdminPublications(teamId);
            }

            Publication publication = optionalPublication.get();
            builder.withContent(publication.getContent())
                    .withId(publication.getId())
                    .withTitle(publication.getTitle())
                    .withPublishedAt(publication.getPublishedAt());

            published = publication.getPublishedStatus().equals(PublishedStatus.PUBLISHED);
        }

        NewPublicationForm form = builder.get();

        addGlobalValues(principal, model, "Administration - Modifier la publication", team);
        model.addAttribute("formdata", form);
        model.addAttribute("published", published);
        return "team_admin_publications_new";
    }

    @GetMapping(value = "/delete/{publicationId}")
    public String deletePublication(@PathVariable("teamId") String teamId,
                                    @PathVariable("publicationId") String publicationId,
                                    Principal principal,
                                    Model model) {

        checkAdmin(principal, teamId);

        try {
            publicationService.delete(teamId, publicationId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return redirectToAdminPublications(teamId);
    }

    private String redirectToAdminPublications(String teamId) {
        return "redirect:/" + teamId + "/admin/publications";
    }

}
