package info.tomacla.biketeam.web.admin.map;

import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.Gpx;
import info.tomacla.biketeam.common.Vector;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapRepository;
import info.tomacla.biketeam.domain.map.WindDirection;
import info.tomacla.biketeam.service.FileService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/admin/maps")
public class AdminMapController extends AbstractController {

    @Autowired
    private FileService fileService;

    @Autowired
    private MapRepository mapRepository;


    @GetMapping
    public String getMaps(Principal principal, Model model) {
        addGlobalValues(principal, model, "Administration - Maps");
        model.addAttribute("maps", mapRepository.findAllByOrderByPostedAtDesc());
        return "admin_maps";
    }

    @GetMapping(value = "/{mapId}")
    public String editMap(@PathVariable("mapId") String mapId,
                          Principal principal,
                          Model model) {

        Optional<Map> optionalMap = mapRepository.findById(mapId);
        if (optionalMap.isEmpty()) {
            return "redirect:/admin/maps";
        }

        Map map = optionalMap.get();

        NewMapForm form = NewMapForm.builder()
                .withId(map.getId())
                .withName(map.getName())
                .withVisible(map.isVisible())
                .withTags(map.getTags())
                .get();

        addGlobalValues(principal, model, "Administration - Modifier la map");
        model.addAttribute("formdata", form);
        model.addAttribute("map", map);
        model.addAttribute("tags", mapRepository.findAllDistinctTags());
        return "admin_maps_new";

    }

    @PostMapping(value = "/{mapId}")
    public String editMap(@PathVariable("mapId") String mapId,
                          Principal principal,
                          Model model,
                          NewMapForm form) {

        try {

            Optional<Map> optionalMap = mapRepository.findById(mapId);
            if (optionalMap.isEmpty()) {
                return "redirect:/admin/maps";
            }

            Map map = optionalMap.get();
            map.setName(form.getName());
            map.setVisible(form.isVisible());
            map.setTags(form.getTags());

            mapRepository.save(map);

            return "redirect:/admin/maps";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Modifier la map");
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            model.addAttribute("tags", mapRepository.findAllDistinctTags());
            return "admin_maps_new";
        }

    }


    @PostMapping(value = "/new")
    public String newMapGpx(Model model,
                            Principal principal,
                            @RequestParam("file") MultipartFile file) {


        try {

            Path gpx = fileService.getTempFileFromInputStream(file.getInputStream());

            Gpx.GpxDescriptor gpxParsed = Gpx.parse(gpx);
            Path staticMapImage = Gpx.staticImage(gpx, configurationService.getSiteIntegration().getMapBoxAPIKey());

            Vector windVector = gpxParsed.getWind();

            Map newMap = new Map(file.getOriginalFilename(),
                    gpxParsed.getLength(),
                    gpxParsed.getPositiveElevation(),
                    gpxParsed.getNegativeElevation(),
                    new ArrayList<>(),
                    gpxParsed.getStart(),
                    gpxParsed.getEnd(),
                    WindDirection.findDirectionFromVector(windVector),
                    gpxParsed.isCrossing(),
                    false);

            fileService.store(gpx, FileRepositories.GPX_FILES, newMap.getId() + ".gpx");
            fileService.store(staticMapImage, FileRepositories.MAP_IMAGES, newMap.getId() + ".png");

            mapRepository.save(newMap);

            return "redirect:/admin/maps/" + newMap.getId();


        } catch (Exception e) {

            addGlobalValues(principal, model, "Administration - Maps");
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("maps", mapRepository.findAllByOrderByPostedAtDesc());
            return "admin_maps";

        }

    }

    @GetMapping(value = "/delete/{mapId}")
    public String deleteMap(@PathVariable("mapId") String mapId,
                            Model model) {

        try {
            mapRepository.findById(mapId).ifPresent(map -> mapRepository.delete(map));

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        }

        return "redirect:/admin/maps";
    }


}
