package info.tomacla.biketeam.web.team.place;

import info.tomacla.biketeam.domain.place.Place;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.PlaceService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/admin/places")
public class AdminTeamPlaceController extends AbstractController {

    @Autowired
    private PlaceService placeService;

    @GetMapping
    public String getPlaces(@PathVariable("teamId") String teamId,
                            @ModelAttribute("error") String error,
                            Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "Administration - Lieux", team);
        model.addAttribute("places", placeService.listPlaces(team.getId()));
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_places";
    }

    @GetMapping(value = "/{placeId}")
    public String editPlace(@PathVariable("teamId") String teamId,
                            @PathVariable("placeId") String placeId,
                            @ModelAttribute("error") String error,
                            Principal principal,
                            Model model) {

        final Team team = checkTeam(teamId);

        NewPlaceForm.NewPlaceFormBuilder builder = NewPlaceForm.builder();

        if (!placeId.equals("new")) {

            Optional<Place> optionalPlace = placeService.get(team.getId(), placeId);
            if (optionalPlace.isEmpty()) {
                return viewHandler.redirect(team, "/admin/places");
            }

            Place place = optionalPlace.get();
            builder.withId(place.getId())
                    .withName(place.getName())
                    .withAddress(place.getAddress())
                    .withLink(place.getLink())
                    .withPoint(place.getPoint())
                    .withStartPlace(place.isStartPlace())
                    .withEndPlace(place.isEndPlace());

        }

        NewPlaceForm form = builder.get();

        addGlobalValues(principal, model, "Administration - Modifier le lieu", team);
        model.addAttribute("formdata", form);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }

        return "team_admin_places_new";

    }

    @PostMapping(value = "/{placeId}")
    public RedirectView editMap(@PathVariable("teamId") String teamId,
                                @PathVariable("placeId") String placeId,
                                Principal principal, Model model,
                                RedirectAttributes attributes,
                                NewPlaceForm form) {

        final Team team = checkTeam(teamId);

        try {

            boolean isNew = placeId.equals("new");

            final NewPlaceForm.NewPlaceFormParser parser = form.parser();
            Place target;
            if (!isNew) {
                Optional<Place> optionalPlace = placeService.get(team.getId(), placeId);
                if (optionalPlace.isEmpty()) {
                    return viewHandler.redirectView(team, "/admin/places");
                }

                target = optionalPlace.get();

            } else {
                target = new Place();
                target.setTeamId(team.getId());
            }

            target.setName(parser.getName());
            target.setAddress(parser.getAddress());
            target.setLink(parser.getLink());
            target.setPoint(parser.getPoint());
            target.setEndPlace(parser.isEndPlace());
            target.setStartPlace(parser.isStartPlace());

            placeService.save(target);

            return viewHandler.redirectView(team, "/admin/places");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/places/" + placeId);
        }

    }

    @GetMapping(value = "/delete/{placeId}")
    public RedirectView deletePlace(@PathVariable("teamId") String teamId,
                                    @PathVariable("placeId") String placeId,
                                    Principal principal, Model model,
                                    RedirectAttributes attributes) {

        final Team team = checkTeam(teamId);

        try {
            placeService.delete(team.getId(), placeId);
            return viewHandler.redirectView(team, "/admin/places");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/places");
        }

    }

}
