package info.tomacla.biketeam.web.user;

import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.web.AbstractController;
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
import java.util.Optional;

@Controller
@RequestMapping(value = "/users")
public class UserController extends AbstractController {

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

    @GetMapping(value = "/me/delete")
    public String deleteMyself(Principal principal, Model model) {

        Optional<User> optionalConnectedUser = getUserFromPrincipal(principal);
        if (optionalConnectedUser.isEmpty()) {
            return "redirect:/";
        }

        final User user = optionalConnectedUser.get();

        userService.delete(user);

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
