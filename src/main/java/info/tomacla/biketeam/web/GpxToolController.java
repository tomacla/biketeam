package info.tomacla.biketeam.web;

import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.garmin.GarminAuthService;
import info.tomacla.biketeam.service.garmin.GarminCourseService;
import info.tomacla.biketeam.service.garmin.GarminMapDescriptor;
import info.tomacla.biketeam.service.garmin.GarminToken;
import info.tomacla.biketeam.service.gpx.GpxDownloadClient;
import info.tomacla.biketeam.service.gpx.GpxService;
import info.tomacla.biketeam.service.gpx.MapData;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

    @Autowired
    private GarminAuthService garminAuthService;

    @Autowired
    private GarminCourseService garminCourseService;

    @Autowired
    private GpxDownloadClient gpxDownloadClient;

    @GetMapping(value = {"", "/"})
    public ModelAndView root(@RequestParam(name = "gpx", required = false) String gpx, Principal principal, Model model) {

        if (StringUtils.hasText(gpx)) {
            try {
                final Path targetFile = fileService.getTempFile("download", ".gpx");
                gpxDownloadClient.downloadGPX(targetFile.toFile(), gpx);
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

            if (uuid.equals("raw")) {
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

    @ResponseBody
    @RequestMapping(value = "/{uuid}/data", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<MapData> getData(@PathVariable("uuid") String uuid) {
        Optional<MapData> mapData = gpxService.getMapData(uuid);
        if (mapData.isPresent()) {

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("map-data.json")
                    .build());

            return new ResponseEntity<>(
                    mapData.get(),
                    headers,
                    HttpStatus.OK
            );

        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to calculation elevation profile : " + uuid);
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

    @PostMapping(value = "/merge")
    public ModelAndView mergeGpx(Principal principal, Model model,
                                 @RequestParam("file1") MultipartFile file1,
                                 @RequestParam("file2") MultipartFile file2) {

        try {

            Path gpxPath1 = fileService.getTempFileFromInputStream(file1.getInputStream());
            Path gpxPath2 = fileService.getTempFileFromInputStream(file2.getInputStream());

            String uuid = gpxService.parseAndStoreStandalone(gpxPath1, gpxPath2);

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

    @RequestMapping(value = "/{uuid}/garmin", method = RequestMethod.GET)
    public void uploadMapGarmin(HttpServletRequest request,
                                HttpServletResponse response,
                                HttpSession session,
                                @PathVariable("uuid") String uuid) throws Exception {

        GarminToken token = garminAuthService.queryToken(request, response, session);
        if (token != null) {
            Optional<StandaloneGpx> standalone = gpxService.getStandalone(uuid);
            if (standalone.isPresent()) {

                try {
                    Path file = fileService.getFile(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx");

                    StandaloneGpx standaloneGpx = standalone.get();

                    GarminMapDescriptor descriptor = new GarminMapDescriptor(
                            file,
                            uuid,
                            MapType.ROAD,
                            standaloneGpx.getLength(),
                            standaloneGpx.getPositiveElevation(),
                            standaloneGpx.getNegativeElevation()
                    );

                    String url = garminCourseService.upload(request, response, session, token, descriptor);
                    if (url != null) {
                        response.sendRedirect(url);
                    }


                } catch (IOException e) {
                    throw new ServerErrorException("Error while reading gpx : " + uuid, e);
                }

            }

        }

    }

    @ResponseBody
    @RequestMapping(value = "/{uuid}/fit", method = RequestMethod.GET, produces = "application/fit")
    public ResponseEntity<byte[]> getFitFile(@PathVariable("uuid") String uuid) {

        if (fileService.fileExists(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx")) {
            try {
                Path file = fileService.getFile(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx");

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "application/vnd.ant.fit");
                headers.setContentDisposition(ContentDisposition.builder("inline")
                        .filename(uuid + ".fit")
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
