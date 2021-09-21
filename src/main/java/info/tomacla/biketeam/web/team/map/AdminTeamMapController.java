package info.tomacla.biketeam.web.team.map;

import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.web.AbstractController;
import liquibase.util.file.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
                          Principal principal, Model model) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        addGlobalValues(principal, model, "Administration - Maps", team);
        model.addAttribute("maps", mapService.listMaps(team.getId()));
        return "team_admin_maps";
    }

    @GetMapping(value = "/{mapId}")
    public String editMap(@PathVariable("teamId") String teamId,
                          @PathVariable("mapId") String mapId,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        Optional<Map> optionalMap = mapService.get(team.getId(), mapId);
        if (optionalMap.isEmpty()) {
            return redirectToAdminMaps(team);
        }

        Map map = optionalMap.get();

        NewMapForm form = NewMapForm.builder()
                .withId(map.getId())
                .withName(map.getName())
                .withVisible(map.isVisible())
                .withTags(map.getTags())
                .withType(map.getType())
                .get();

        addGlobalValues(principal, model, "Administration - Modifier la map", team);
        model.addAttribute("formdata", form);
        model.addAttribute("map", map);
        model.addAttribute("tags", mapService.listTags(team.getId()));
        return "team_admin_maps_new";

    }

    @PostMapping(value = "/{mapId}")
    public String editMap(@PathVariable("teamId") String teamId,
                          @PathVariable("mapId") String mapId,
                          Principal principal,
                          Model model,
                          NewMapForm form) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        try {

            Optional<Map> optionalMap = mapService.get(team.getId(), mapId);
            if (optionalMap.isEmpty()) {
                return redirectToAdminMaps(team);
            }

            final NewMapForm.NewMapFormParser parser = form.parser();

            Map map = optionalMap.get();
            map.setName(parser.getName());
            map.setVisible(parser.isVisible());
            map.setTags(parser.getTags());
            map.setType(parser.getType());

            mapService.save(map);

            return redirectToAdminMaps(team);

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Modifier la map", team);
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            model.addAttribute("tags", mapService.listTags(team.getId()));
            return "team_admin_maps_new";
        }

    }


    @PostMapping(value = "/new")
    public String newMapGpx(@PathVariable("teamId") String teamId,
                            Model model,
                            Principal principal,
                            @RequestParam("file") MultipartFile file) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        try {

            final Map newMap = mapService.save(
                    team,
                    file.getInputStream(),
                    FilenameUtils.removeExtension(file.getOriginalFilename()),
                    null
            );
            return redirectToAdminMap(team, newMap.getId());

        } catch (Exception e) {

            addGlobalValues(principal, model, "Administration - Maps", team);
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("maps", mapService.listMaps(team.getId()));
            return "team_admin_maps";

        }

    }

    @GetMapping(value = "/delete/{mapId}")
    public String deleteMap(@PathVariable("teamId") String teamId,
                            @PathVariable("mapId") String mapId,
                            Principal principal,
                            Model model) {

        final Team team = checkTeam(teamId);
        checkAdmin(principal, team.getId());

        try {
            mapService.delete(team.getId(), mapId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return redirectToAdminMaps(team);
    }

    private String redirectToAdminMaps(Team team) {
        return createRedirect(team, "/admin/maps");
    }

    private String redirectToAdminMap(Team team, String mapId) {
        return createRedirect(team, "/admin/maps/" + mapId);
    }


}
