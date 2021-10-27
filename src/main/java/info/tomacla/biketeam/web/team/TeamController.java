package info.tomacla.biketeam.web.team;

import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.common.ImageDescriptor;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamConfiguration;
import info.tomacla.biketeam.domain.team.WebPage;
import info.tomacla.biketeam.domain.user.Role;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.HeatmapService;
import info.tomacla.biketeam.service.ThumbnailService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;

import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}")
public class TeamController extends AbstractController {

    @Autowired
    private HeatmapService heatmapService;

    @Autowired
    private ThumbnailService thumbnailService;

    @GetMapping
    public String getFeed(@PathVariable("teamId") String teamId,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        final TeamConfiguration teamConfiguration = team.getConfiguration();
        if (teamConfiguration.getDefaultPage().equals(WebPage.MAPS)) {
            return redirectToMaps(team);
        }
        if (teamConfiguration.getDefaultPage().equals(WebPage.RIDES)) {
            return redirectToRides(team);
        }
        if (teamConfiguration.getDefaultPage().equals(WebPage.TRIPS)) {
            return redirectToTrips(team);
        }

        addGlobalValues(principal, model, team.getName(), team);
        model.addAttribute("feed", teamService.listFeed(team.getId()));
        model.addAttribute("hasHeatmap", team.getIntegration().isHeatmapDisplay() && heatmapService.get(team.getId()).isPresent());
        return "team_root";
    }

    @GetMapping(value = "/join")
    public String joinTeam(@PathVariable("teamId") String teamId,
                           Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isPresent()) {

            User connectedUser = optionalConnectedUser.get();

            if (!team.isAdminOrMember(connectedUser.getId())) {
                team.addRole(connectedUser, Role.MEMBER);
                teamService.save(team);
            }

        }

        return redirectToFeed(team);
    }

    @GetMapping(value = "/leave")
    public String leaveTeam(@PathVariable("teamId") String teamId,
                            Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);

        if (optionalConnectedUser.isPresent()) {
            User connectedUser = optionalConnectedUser.get();
            if (team.isAdminOrMember(connectedUser.getId())) {
                team.removeRole(connectedUser);
                teamService.save(team);
            }
        }

        return redirectToFeed(team);
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
