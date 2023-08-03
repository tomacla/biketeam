package info.tomacla.biketeam.web;

import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.gpx.GpxDownloadClient;
import info.tomacla.biketeam.service.gpx.GpxService;
import info.tomacla.biketeam.service.gpx.StandaloneGpx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/gpxtool")
public class GpxToolController extends AbstractController {

    @Autowired
    private GpxService gpxService;

    @Autowired
    private FileService fileService;

    @GetMapping
    public ModelAndView root(@RequestParam(name = "gpx", required = false) String gpx, Principal principal, Model model) {

        if (StringUtils.hasText(gpx)) {
            try {
                final Path targetFile = fileService.getTempFile("download", ".gpx");
                GpxDownloadClient.downloadGPX(targetFile.toFile(), gpx);
                String uuid = gpxService.parseAndStoreStandalone(targetFile);
                return new ModelAndView(new RedirectView("/gpxtool/" + uuid, false, false, false));
            } catch (Exception e) {
                addGlobalValues(principal, model, "GPX Tool", null);
                model.addAttribute("errors", List.of("Unable to parse GPX"));
                return new ModelAndView("gpxtool-root", model.asMap());
            }
        }

        addGlobalValues(principal, model, "GPX Tool", null);
        return new ModelAndView("gpxtool-root", model.asMap());
    }

    @GetMapping(value = "/{uuid}")
    public ModelAndView displayGpx(@PathVariable("uuid") String uuid, Principal principal, Model model) {

        try {

            if(uuid.equals("raw")) {
                addGlobalValues(principal, model, "GPX Tool", null);
                model.addAttribute("_fullSize", true);
                model.addAttribute("gpxuuid", uuid);

                return new ModelAndView("gpxtool-map", model.asMap());
            }

            Optional<StandaloneGpx> standalone = gpxService.getStandalone(uuid);
            if (standalone.isPresent()) {

                addGlobalValues(principal, model, "GPX Tool", null);
                model.addAttribute("_fullSize", true);
                model.addAttribute("gpxuuid", uuid);
                model.addAttribute("gpx", standalone.get());

                return new ModelAndView("gpxtool-map", model.asMap());

            }

            return new ModelAndView(new RedirectView("gpxtool-root", false, false, false));

        } catch (Exception e) {
            addGlobalValues(principal, model, "GPX Tool", null);
            model.addAttribute("errors", List.of("Unable to parse GPX"));
            return new ModelAndView("gpxtool-root", model.asMap());
        }

    }

    @PostMapping
    public ModelAndView parseGPX(Principal principal, Model model, @RequestParam("file") MultipartFile file) {

        try {

            Path gpxPath = fileService.getTempFileFromInputStream(file.getInputStream());

            String uuid = gpxService.parseAndStoreStandalone(gpxPath);

            return new ModelAndView(new RedirectView("/gpxtool/" + uuid, false, false, false));

        } catch (Exception e) {
            addGlobalValues(principal, model, "GPX Tool", null);
            model.addAttribute("errors", List.of("Unable to parse GPX"));
            return new ModelAndView("gpxtool-root", model.asMap());
        }

    }

    @ResponseBody
    @RequestMapping(value = "/{uuid}/gpx", method = RequestMethod.GET, produces = "application/gpx+xml")
    public ResponseEntity<byte[]> downloadGpx(@PathVariable("uuid") String uuid) {

        if (fileService.fileExists(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx")) {
            try {
                Path file = fileService.getFile(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx");

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "application/gpx+xml");
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(uuid + ".gpx")
                        .build());

                return new ResponseEntity<>(
                        Files.readAllBytes(file),
                        headers,
                        HttpStatus.OK
                );

            } catch (IOException e) {
                throw new ServerErrorException("Error while reading gpx : " + uuid, e);
            }

        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find gpx : " + uuid);
    }

}
