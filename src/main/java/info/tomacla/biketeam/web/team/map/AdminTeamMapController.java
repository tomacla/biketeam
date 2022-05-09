package info.tomacla.biketeam.web.team.map;

import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.MapService;
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
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/{teamId}/admin/maps")
public class AdminTeamMapController extends AbstractController {

    @Autowired
    private MapService mapService;

    @GetMapping
    public String getMaps(@PathVariable("teamId") String teamId,
                          @ModelAttribute("error") String error,
                          Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "Administration - Maps", team);
        model.addAttribute("maps", mapService.listMaps(team.getId()));
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_maps";
    }

    @GetMapping(value = "/{mapId}")
    public String editMap(@PathVariable("teamId") String teamId,
                          @PathVariable("mapId") String mapId,
                          @ModelAttribute("error") String error,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);

        Optional<Map> optionalMap = mapService.get(team.getId(), mapId);
        if (optionalMap.isEmpty()) {
            return viewHandler.redirect(team, "/admin/maps");
        }

        Map map = optionalMap.get();

        NewMapForm form = NewMapForm.builder()
                .withId(map.getId())
                .withName(map.getName())
                .withTags(map.getTags())
                .withType(map.getType())
                .withPermalink(map.getPermalink())
                .get();

        addGlobalValues(principal, model, "Administration - Modifier la map", team);
        model.addAttribute("formdata", form);
        model.addAttribute("map", map);
        model.addAttribute("tags", mapService.listTags(team.getId()));
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_maps_new";

    }

    @PostMapping(value = "/{mapId}")
    public RedirectView editMap(@PathVariable("teamId") String teamId,
                                @PathVariable("mapId") String mapId,
                                Principal principal, Model model,
                                RedirectAttributes attributes,
                                NewMapForm form) {

        final Team team = checkTeam(teamId);

        try {

            Optional<Map> optionalMap = mapService.get(team.getId(), mapId);
            if (optionalMap.isEmpty()) {
                return viewHandler.redirectView(team, "/admin/maps");
            }

            final NewMapForm.NewMapFormParser parser = form.parser();

            Map map = optionalMap.get();
            map.setName(parser.getName());
            map.setTags(parser.getTags());
            map.setType(parser.getType());
            map.setPermalink(parser.getPermalink());

            if (parser.getFile().isPresent()) {
                MultipartFile uploadedFile = parser.getFile().get();
                mapService.replaceGpx(team, map, uploadedFile.getInputStream());
            }

            mapService.save(map, true);

            return viewHandler.redirectView(team, "/admin/maps");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/maps/" + mapId);
        }

    }

    @PostMapping(value = "/new")
    public RedirectView newMapGpx(@PathVariable("teamId") String teamId,
                                  Model model,
                                  Principal principal,
                                  RedirectAttributes attributes,
                                  @RequestParam("file") MultipartFile file) {

        final Team team = checkTeam(teamId);

        try {

            final Map newMap = mapService.createFromGpx(
                    team,
                    file.getInputStream(),
                    null,
                    null
            );

            newMap.setPermalink(mapService.getPermalink(newMap.getName()));
            mapService.save(newMap);

            return viewHandler.redirectView(team, "/admin/maps/" + newMap.getId());

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/maps");
        }

    }

    @GetMapping(value = "/delete/{mapId}")
    public RedirectView deleteMap(@PathVariable("teamId") String teamId,
                                  @PathVariable("mapId") String mapId,
                                  Principal principal, Model model,
                                  RedirectAttributes attributes) {

        final Team team = checkTeam(teamId);

        try {
            mapService.delete(team.getId(), mapId);
            return viewHandler.redirectView(team, "/admin/maps");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/maps");
        }

    }

}
