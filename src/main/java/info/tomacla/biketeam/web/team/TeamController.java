package info.tomacla.biketeam.web.team;

import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamConfiguration;
import info.tomacla.biketeam.domain.team.WebPage;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.service.UserRoleService;
import info.tomacla.biketeam.service.file.ThumbnailService;
import info.tomacla.biketeam.service.heatmap.HeatmapService;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "/{teamId}")
public class TeamController extends AbstractController {

    @Autowired
    private HeatmapService heatmapService;

    @Autowired
    private ThumbnailService thumbnailService;

    @Autowired
    private UserRoleService userRoleService;

    @GetMapping
    public String getFeed(@PathVariable("teamId") String teamId,
                          @ModelAttribute("error") String error,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        final TeamConfiguration teamConfiguration = team.getConfiguration();
        if (teamConfiguration.getDefaultPage().equals(WebPage.MAPS)) {
            return viewHandler.redirect(team, "/maps");
        }
        if (teamConfiguration.getDefaultPage().equals(WebPage.RIDES)) {
            return viewHandler.redirect(team, "/rides");
        }
        if (teamConfiguration.getDefaultPage().equals(WebPage.TRIPS)) {
            return viewHandler.redirect(team, "/trips");
        }

        addGlobalValues(principal, model, team.getName(), team);
        model.addAttribute("feed", teamService.listFeed(team.getId()));
        model.addAttribute("users", team.getRoles().stream().map(UserRole::getUser).collect(Collectors.toSet()));
        model.addAttribute("hasHeatmap", team.getIntegration().isHeatmapDisplay() && heatmapService.get(team.getId()).isPresent());
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_root";
    }

    @GetMapping(value = "/join")
    public RedirectView joinTeam(@PathVariable("teamId") String teamId,
                                 RedirectAttributes attributes,
                                 Principal principal,
                                 Model model) {

        final Team team = checkTeam(teamId);

        try {
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
            if (optionalConnectedUser.isPresent()) {
                User connectedUser = optionalConnectedUser.get();
                if (!team.isMember(connectedUser)) {
                    userRoleService.save(new UserRole(team, connectedUser, Role.MEMBER));
                }
            }
            return viewHandler.redirectView(team, "/");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/");
        }

    }

    @GetMapping(value = "/leave")
    public RedirectView leaveTeam(@PathVariable("teamId") String teamId,
                                  RedirectAttributes attributes,
                                  Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        try {
            Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

            if (optionalConnectedUser.isPresent()) {
                User connectedUser = optionalConnectedUser.get();
                if (team.isMember(connectedUser)) {
                    userRoleService.delete(team, connectedUser);
                }
            }

            return viewHandler.redirectView(team, "/");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/");
        }
    }

    @ResponseBody
    @RequestMapping(value = "/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getRideImage(@PathVariable("teamId") String teamId) {
        final Optional<ImageDescriptor> image = teamService.getImage(teamId);
        if (image.isPresent()) {
            try {

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", image.get().getExtension().getMediaType());
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(teamId + image.get().getExtension().getExtension())
                        .build());

                return new ResponseEntity<>(
                        Files.readAllBytes(image.get().getPath()),
                        headers,
                        HttpStatus.OK
                );
            } catch (IOException e) {
                throw new ServerErrorException("Error while reading team image : " + teamId, e);
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find team image : " + teamId);

    }

    @ResponseBody
    @RequestMapping(value = "/heatmap", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getHeatmap(@PathVariable("teamId") String teamId,
                                             @RequestParam(name = "width", defaultValue = "-1", required = false) int targetWidth) {
        final Optional<ImageDescriptor> image = heatmapService.get(teamId);
        if (image.isPresent()) {
            try {

                final ImageDescriptor targetImage = image.get();
                final FileExtension targetImageExtension = targetImage.getExtension();

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", targetImageExtension.getMediaType());
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(teamId + targetImageExtension.getExtension())
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
                throw new ServerErrorException("Error while reading team image : " + teamId, e);
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find team image : " + teamId);

    }

}
