package info.tomacla.biketeam.web.admin.map;

import info.tomacla.biketeam.domain.map.Map;
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
@RequestMapping(value = "/admin/maps")
public class AdminMapController extends AbstractController {

    @Autowired
    private MapService mapService;


    @GetMapping
    public String getMaps(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Maps");
        model.addAttribute("maps", mapService.listMaps());
        return "admin_maps";
    }

    @GetMapping(value = "/{mapId}")
    public String editMap(@PathVariable("mapId") String mapId,
                          Principal principal,
                          Model model) {

        Optional<Map> optionalMap = mapService.get(mapId);
        if (optionalMap.isEmpty()) {
            return "redirect:/admin/maps";
        }

        Map map = optionalMap.get();

        NewMapForm form = NewMapForm.builder()
                .withId(map.getId())
                .withName(map.getName())
                .withVisible(map.isVisible())
                .withTags(map.getTags())
                .withType(map.getType())
                .get();

        addGlobalValues(principal, model, "Administration - Modifier la map");
        model.addAttribute("formdata", form);
        model.addAttribute("map", map);
        model.addAttribute("tags", mapService.listTags());
        return "admin_maps_new";

    }

    @PostMapping(value = "/{mapId}")
    public String editMap(@PathVariable("mapId") String mapId,
                          Principal principal,
                          Model model,
                          NewMapForm form) {

        try {

            Optional<Map> optionalMap = mapService.get(mapId);
            if (optionalMap.isEmpty()) {
                return "redirect:/admin/maps";
            }

            final NewMapForm.NewMapFormParser parser = form.parser();

            Map map = optionalMap.get();
            map.setName(parser.getName());
            map.setVisible(parser.isVisible());
            map.setTags(parser.getTags());
            map.setType(parser.getType());

            mapService.save(map);

            return "redirect:/admin/maps";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Modifier la map");
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            model.addAttribute("tags", mapService.listTags());
            return "admin_maps_new";
        }

    }


    @PostMapping(value = "/new")
    public String newMapGpx(Model model,
                            Principal principal,
                            @RequestParam("file") MultipartFile file) {


        try {

            final Map newMap = mapService.save(
                    file.getInputStream(),
                    FilenameUtils.removeExtension(file.getOriginalFilename()),
                    null
            );
            return "redirect:/admin/maps/" + newMap.getId();

        } catch (Exception e) {

            addGlobalValues(principal, model, "Administration - Maps");
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("maps", mapService.listMaps());
            return "admin_maps";

        }

    }

    @GetMapping(value = "/delete/{mapId}")
    public String deleteMap(@PathVariable("mapId") String mapId,
                            Model model) {

        try {
            mapService.delete(mapId);
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/maps";
    }


}
