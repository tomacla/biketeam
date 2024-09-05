package info.tomacla.biketeam.service.gpx;

import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.geo.Vector;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

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
        if (fileService.fileExists(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx")) {

            Path file = fileService.getFile(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx");
            GPXPath gpxPath = getGPXPath(file, uuid);

            return Optional.of(
                    new StandaloneGpx(Rounder.round2Decimals(Math.round(10.0 * gpxPath.getDist()) / 10000.0),
                            Rounder.round1Decimal(gpxPath.getTotalElevation()),
                            Rounder.round1Decimal(gpxPath.getTotalElevationNegative())
                    )
            );

        }
        return Optional.empty();
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

    public Optional<MapData> getMapData(String uuid) {
        Optional<Path> gpxFile = getGpxFile(uuid);
        if (gpxFile.isPresent()) {
            return Optional.of(getMapData(gpxFile.get()));
        }
        return Optional.empty();
    }

    public Optional<Path> getGpxFile(String uuid) {
        if (fileService.fileExists(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx")) {
            Path file = fileService.getFile(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx");
            return Optional.of(file);
        }
        return Optional.empty();
    }

    public MapData getMapData(Path path) {
        GPXPath gpxPath = getGPXPath(path);
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

            List<MapPoint> mapPoints = new ArrayList<>();
            List<Marker> markers = new ArrayList<>();
            List<Point> points = gpxPath.getPoints();

            Point start = points.get(0);
            Point end = points.get(points.size() - 1);
            markers.add(new Marker(start.getLatDeg(), start.getLonDeg(), "start", "start"));
            markers.add(new Marker(end.getLatDeg(), end.getLonDeg(), "end", "end"));

            double markerDist = 10000;
            double marker = markerDist;
            for (int i = 0; i < points.size(); i++) {
                Point point = points.get(i);

                double dist = point.getDist();
                if (dist > marker) {
                    markers.add(new Marker(point.getLatDeg(), point.getLonDeg(), "step", String.valueOf(Math.round(marker / 1000))));
                    marker += markerDist;
                }

                Double grade = climbGrades.floorEntry(dist).getValue();
                boolean inClimb;
                if (grade == null) {
                    grade = point.getGrade() * 100;
                    inClimb = false;
                } else {
                    inClimb = true;
                }
                mapPoints.add(new MapPoint(
                                i,
                                point.getLatDeg(),
                                point.getLonDeg(),
                                dist / 1000.0,
                                point.getEle(),
                                grade,
                                inClimb
                        )
                );
            }
            return new MapData(getMapInfo(gpxPath), mapPoints, climbs, markers);
        } catch (Exception e) {
            log.error("Error while calculating Elevation profile", e);
            throw new RuntimeException(e);
        }
    }

    private MapInfo getMapInfo(GPXPath gpxPath) {
        return new MapInfo(
                gpxPath.getName(),
                Math.round(gpxPath.getDist() / 100.0) / 10.0,
                gpxPath.getTotalElevation(),
                gpxPath.getTotalElevationNegative()
        );
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
