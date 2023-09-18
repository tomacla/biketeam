package info.tomacla.biketeam.web.user;

import info.tomacla.biketeam.common.data.Country;
import info.tomacla.biketeam.common.data.Timezone;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.Visibility;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.domain.userrole.Role;
import info.tomacla.biketeam.domain.userrole.UserRole;
import info.tomacla.biketeam.security.Authorities;
import info.tomacla.biketeam.service.UserRoleService;
import info.tomacla.biketeam.web.AbstractController;
import info.tomacla.biketeam.web.team.NewTeamForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/users")
public class UserController extends AbstractController {

    @Autowired
    private UserRoleService userRoleService;

    @GetMapping(value = "/me")
    public String getUser(Principal principal,
                          Model model) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        final EditUserForm form = EditUserForm.builder()
                .withEmail(user.getEmail())
                .withStravaId(user.getStravaId())
                .withEmailPublishPublications(user.isEmailPublishPublications())
                .withEmailPublishRides(user.isEmailPublishRides())
                .withEmailPublishTrips(user.isEmailPublishTrips())
                .get();

        addGlobalValues(principal, model, "Mon profil", null);
        model.addAttribute("user", user);
        model.addAttribute("formdata", form);
        return "user";


    }

    @GetMapping(value = "/space")
    public String getSpace(Principal principal,
                           Model model) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        if (user.getTeamId() != null) {
            return "redirect:/" + user.getTeamId();
        }

        addGlobalValues(principal, model, "Créer mon espace", null);
        model.addAttribute("formdata", NewTeamForm.builder()
                .withCity(user.getCity())
                .withCountry(Country.FR.name())
                .withName(user.getIdentity())
                .withId(teamService.getPermalink(user.getIdentity(), 18, true))
                .withTimezone(Timezone.DEFAULT_TIMEZONE)
                .withDescription("Espace personnel de " + user.getIdentity())
                .get());
        model.addAttribute("timezones", getAllAvailableTimeZones());
        return "user_space";


    }

    @PostMapping(value = "/space")
    public String initSpace(NewTeamForm form, Principal principal, Model model) {

        try {

            final User targetAdmin = getUserFromPrincipal(principal).orElseThrow(() -> new IllegalStateException("User not authenticated"));

            final NewTeamForm.NewTeamFormParser parser = form.parser();

            String targetId = parser.getId().toLowerCase();

            teamService.get(targetId).ifPresent(team -> {
                throw new IllegalArgumentException("Team " + team.getId() + " already exists");
            });

            final Team newTeam = new Team();
            newTeam.setId(targetId);
            newTeam.setName(parser.getName());
            newTeam.setCity(parser.getCity());
            newTeam.setCountry(parser.getCountry());
            newTeam.getDescription().setDescription(parser.getDescription());
            newTeam.getConfiguration().setTimezone(parser.getTimezone());
            newTeam.setVisibility(Visibility.USER);

            teamService.save(newTeam, true);

            userRoleService.save(new UserRole(newTeam, targetAdmin, Role.ADMIN));

            targetAdmin.setTeamId(targetId);
            userService.save(targetAdmin);

            addAuthorityToCurrentSession(Authorities.teamAdmin(newTeam.getId()));

            return "redirect:/" + newTeam.getId();

        } catch (Exception e) {
            addGlobalValues(principal, model, "Créer mon espace", null);
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            model.addAttribute("timezones", getAllAvailableTimeZones());
            return "user_space";
        }


    }


    @GetMapping(value = "/me/delete")
    public String deleteMyself(Principal principal, Model model) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        userService.delete(user.getId());

        return "redirect:/logout";

    }

    @PostMapping(value = "/me")
    public String updateUser(Principal principal,
                             Model model,
                             EditUserForm form) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final EditUserForm.EditUserFormParser parser = form.parser();

        final User user = optionalConnectedUser.get();
        user.setEmail(parser.getEmail());
        user.setStravaId(parser.getStravaId());
        user.setEmailPublishPublications(parser.isEmailPublishPublications());
        user.setEmailPublishRides(parser.isEmailPublishRides());
        user.setEmailPublishTrips(parser.isEmailPublishTrips());
        userService.save(user);

        addGlobalValues(principal, model, "Mon profil", null);
        model.addAttribute("user", user);
        model.addAttribute("formdata", form);
        return "user";


    }

    @ResponseBody
    @RequestMapping(value = "/{userId}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getUserController(@PathVariable("userId") String userId) {
        final Optional<ImageDescriptor> image = userService.getImage(userId);
        if (image.isPresent()) {
            try {

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", image.get().getExtension().getMediaType());
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(userId + image.get().getExtension().getExtension())
                        .build());

                return new ResponseEntity<>(
                        Files.readAllBytes(image.get().getPath()),
                        headers,
                        HttpStatus.OK
                );
            } catch (IOException e) {
                // ignore
            }
        }

        try {

            InputStream resourceAsStream = getClass().getResourceAsStream("/static/css/default-user.png");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", FileExtension.PNG.getMediaType());
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename(userId + FileExtension.PNG.getExtension())
                    .build());

            return new ResponseEntity<>(
                    resourceAsStream.readAllBytes(),
                    headers,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find user image : " + userId);
        }

    }

}
