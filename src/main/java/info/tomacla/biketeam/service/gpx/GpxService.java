package info.tomacla.biketeam.service.gpx;

import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.geo.Vector;
import info.tomacla.biketeam.common.json.Json;
import info.tomacla.biketeam.common.math.Rounder;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.map.WindDirection;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.file.FileService;
import io.github.glandais.GPXDataComputer;
import io.github.glandais.GPXPathEnhancer;
import io.github.glandais.fit.FitFileWriter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.climb.Climb;
import io.github.glandais.gpx.climb.ClimbDetector;
import io.github.glandais.gpx.climb.ClimbPart;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.map.TileMapImage;
import io.github.glandais.map.TileMapProducer;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

@Service
public class GpxService {

    private static final Logger log = LoggerFactory.getLogger(GpxService.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private GPXParser gpxParser;

    @Autowired
    private GPXPathEnhancer gpxPathEnhancer;

    @Autowired
    private GPXFileWriter gpxFileWriter;

    @Autowired
    private GPXDataComputer gpxDataComputer;

    @Autowired
    private FitFileWriter fitFileWriter;

    @Autowired
    private GeoJsonFileWriter geoJsonFileWriter;

    @Autowired
    private TileMapProducer tileMapProducer;

    @Autowired
    private ClimbDetector climbDetector;

    @Value("${mapbox.api-key}")
    private String mapBoxAPIKey;

    public Map parseAndStore(Team team, Path gpx, String defaultName, String permalink) {

        try {
            Map newMap = new Map();
            newMap.setTeamId(team.getId());
            newMap.setPermalink(permalink);
            newMap.setType(MapType.ROAD);
            newMap.setTags(team.getConfiguration().getDefaultSearchTags());

            GPXPath gpxPath = prepareMap(gpx, defaultName, newMap, team);

            newMap.setName(gpxPath.getName());

            return newMap;
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse GPX", e);
        }

    }

    public Map parseAndReplace(Team team, Map map, Path gpx) {
        try {
            prepareMap(gpx, map.getName(), map, team);
            return map;
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse GPX", e);
        }
    }

    public String parseAndStoreStandalone(Path... gpx) {

        try {
            String defaultName = UUID.randomUUID().toString();

            GPXPath gpxPath = getGpxPathFromFiles(defaultName, gpx);
            gpxPath.setName(defaultName);
            gpxPathEnhancer.virtualize(gpxPath);

            Path toStoreGpx = getGpx(gpxPath);

            fileService.storeFile(toStoreGpx, FileRepositories.GPXTOOLVIEWER, defaultName + ".gpx");

            return defaultName;
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse GPX", e);
        }

    }

    public Optional<StandaloneGpx> getStandalone(String uuid) {
        try {
            if (fileService.fileExists(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx")) {

                Path file = fileService.getFile(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx");
                GPXPath gpxPath = getGPXPath(file, uuid);

                Path asGeoJson = getAsGeoJson(file);

                return Optional.of(
                        new StandaloneGpx(Rounder.round2Decimals(Math.round(10.0 * gpxPath.getDist()) / 10000.0),
                                Rounder.round1Decimal(gpxPath.getTotalElevation()),
                                Rounder.round1Decimal(gpxPath.getTotalElevationNegative()),
                                Json.serialize(getElevationProfile(gpxPath)),
                                FileUtils.readFileToString(asGeoJson.toFile(), StandardCharsets.UTF_8))
                );

            }
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private GPXPath getGpxPathFromFiles(String defaultName, Path... gpx) {

        GPXPath gpxPath = getGPXPath(gpx[0], defaultName);
        if (gpx.length > 1) {
            for (int i = 1; i < gpx.length; i++) {
                GPXPath tgpxPath = getGPXPath(gpx[i], defaultName);
                gpxPath.getPoints().addAll(tgpxPath.getPoints());
                gpxPath.computeArrays(ValueKind.source);
            }
        }
        return gpxPath;

    }

    private GPXPath prepareMap(final Path gpx, final String defaultName, final Map map, final Team team) {
        GPXPath gpxPath = getGPXPath(gpx, defaultName);
        gpxPathEnhancer.virtualize(gpxPath);

        Path storedStaticMap = getStaticMap(gpxPath);
        Path storedGpx = getGpx(gpxPath);

        io.github.glandais.util.Vector windRaw = gpxDataComputer.getWind(gpxPath);
        Vector wind = new Vector(windRaw.getX(), windRaw.getY());
        boolean crossing = gpxDataComputer.isCrossing(gpxPath);

        List<Point> points = gpxPath.getPoints();
        io.github.glandais.gpx.Point startPoint = points.get(0);
        io.github.glandais.gpx.Point endPoint = points.get(points.size() - 1);

        info.tomacla.biketeam.common.geo.Point start = new info.tomacla.biketeam.common.geo.Point(startPoint.getLatDeg(), startPoint.getLonDeg());
        info.tomacla.biketeam.common.geo.Point end = new info.tomacla.biketeam.common.geo.Point(endPoint.getLatDeg(), endPoint.getLonDeg());

        map.setLength(Rounder.round2Decimals(Math.round(10.0 * gpxPath.getDist()) / 10000.0));
        map.setPostedAt(LocalDate.now(team.getZoneId()));
        map.setPositiveElevation(Rounder.round1Decimal(gpxPath.getTotalElevation()));
        map.setNegativeElevation(Rounder.round1Decimal(gpxPath.getTotalElevationNegative()));
        map.setStartPoint(start);
        map.setEndPoint(end);
        map.setWindDirection(WindDirection.findDirectionFromVector(wind));
        map.setCrossing(crossing);

        fileService.storeFile(storedGpx, FileRepositories.GPX_FILES, team.getId(), map.getId() + ".gpx");
        fileService.storeFile(storedStaticMap, FileRepositories.MAP_IMAGES, team.getId(), map.getId() + ".png");
        return gpxPath;
    }

    public void delete(Map map) {
        fileService.deleteFile(FileRepositories.GPX_FILES, map.getTeamId(), map.getId() + ".gpx");
        fileService.deleteFile(FileRepositories.MAP_IMAGES, map.getTeamId(), map.getId() + ".png");
    }

    public void refresh(Map map) {
        this.refresh(map, true, true);
    }

    public void refresh(Map map, boolean gpx, boolean image) {

        GPXPath gpxPath = getGPXPath(fileService.getFile(FileRepositories.GPX_FILES, map.getTeamId(), map.getId() + ".gpx"), map.getName());

        if (gpx) {
            Path storedGpx = getGpx(gpxPath);
            fileService.storeFile(storedGpx, FileRepositories.GPX_FILES, map.getTeamId(), map.getId() + ".gpx");
        }

        if (image) {
            Path storedStaticMap = getStaticMap(gpxPath);
            fileService.storeFile(storedStaticMap, FileRepositories.MAP_IMAGES, map.getTeamId(), map.getId() + ".png");
        }

    }

    public Path getAsFit(Path gpx) {
        try {
            Path fit = fileService.getTempFile("gpxsimplified", ".fit");
            fitFileWriter.writeFitFile(getGPXPath(gpx), fit.toFile());
            return fit;
        } catch (Exception e) {
            log.error("Error while creating FIT", e);
            throw new RuntimeException(e);
        }
    }

    public Path getAsGeoJson(Path gpx) {
        try {
            Path geojson = fileService.getTempFile("gpxsimplified", ".json");
            geoJsonFileWriter.writeGeoJsonFile(getGPXPath(gpx), geojson.toFile());
            return geojson;
        } catch (Exception e) {
            log.error("Error while creating GEOJSON", e);
            throw new RuntimeException(e);
        }
    }

    public List<java.util.Map<String, Object>> getElevationProfile(Path gpx) {
        try {
            return getElevationProfile(getGPXPath(gpx));
        } catch (Exception e) {
            log.error("Error while calculating GEOJSON", e);
            throw new RuntimeException(e);
        }
    }


    private List<java.util.Map<String, Object>> getElevationProfile(GPXPath gpxPath) {
        try {
            List<Climb> climbs = climbDetector.getClimbs(gpxPath);
            NavigableMap<Double, Double> climbGrades = new TreeMap<>();
            climbGrades.put(0.0, null);
            for (Climb climb : climbs) {
                for (ClimbPart climbPart : climb.parts()) {
                    climbGrades.put(climb.startDist() + climbPart.startDist(), climbPart.grade());
                }
                climbGrades.put(climb.endDist(), null);
            }

            List<java.util.Map<String, Object>> result = new ArrayList<>();

            for (int i = 0; i < gpxPath.getPoints().size(); i++) {
                Point point = gpxPath.getPoints().get(i);

                Double grade = climbGrades.floorEntry(point.getDist()).getValue();
                boolean inClimb;
                if (grade == null) {
                    grade = point.getGrade() * 100;
                    inClimb = false;
                } else {
                    inClimb = true;
                }
                result.add(
                        java.util.Map.of("index", i,
                                "x", point.getDist() / 1000.0,
                                "y", point.getEle(),
                                "lat", point.getLatDeg(),
                                "lng", point.getLonDeg(),
                                "grade", grade,
                                "inClimb", inClimb
                        )
                );
            }
            return result;
        } catch (Exception e) {
            log.error("Error while calculating Elevation profile", e);
            throw new RuntimeException(e);
        }
    }

    private GPXPath getGPXPath(Path path) {
        return getGPXPath(path, null);
    }

    private GPXPath getGPXPath(Path path, String defaultName) {
        GPXPath gpxPath;
        try {
            List<GPXPath> paths = gpxParser.parsePaths(path.toFile(), defaultName);
            if (paths.size() == 1) {
                gpxPath = paths.get(0);
                if (gpxPath.getPoints().size() < 2) {
                    throw new IllegalArgumentException("0 or 1 points in path");
                }
            } else {
                throw new IllegalArgumentException("0 or more than 1 path found");
            }
        } catch (Exception e) {
            log.error("Error while parsing GPX", e);
            throw new RuntimeException(e);
        }
        if (defaultName != null) {
            gpxPath.setName(defaultName);
        }
        return gpxPath;
    }

    private Path getGpx(GPXPath gpxPath) {
        try {
            Path gpx = fileService.getTempFile("gpxsimplified", ".gpx");
            gpxFileWriter.writeGpxFile(List.of(gpxPath), gpx.toFile());
            return gpx;
        } catch (Exception e) {
            log.error("Error while creating FIT", e);
            throw new RuntimeException(e);
        }
    }

    private Path getStaticMap(GPXPath path) {
        try {
            String tileUrl = "https://api.mapbox.com/styles/v1/mapbox/outdoors-v11/tiles/256/{z}/{x}/{y}?access_token=" + mapBoxAPIKey;
            TileMapImage tileMap = tileMapProducer.createTileMap(path, tileUrl, 0, 768, 512);
            Path staticMap = fileService.getTempFile("staticmap", ".png");
            ImageIO.write(tileMap.getImage(), "png", staticMap.toFile());
            return staticMap;
        } catch (IOException e) {
            log.error("Error while getting static map", e);
            throw new RuntimeException(e);
        }
    }

}
