package info.tomacla.biketeam.service.gpx;

import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.math.Rounder;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.WindDirection;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.file.FileService;
import io.github.glandais.gpx.climb.Climb;
import io.github.glandais.gpx.climb.ClimbDetector;
import io.github.glandais.gpx.data.*;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.io.write.FitFileWriter;
import io.github.glandais.gpx.io.write.GPXFileWriter;
import io.github.glandais.gpx.map.TileMapProducer;
import io.github.glandais.gpx.util.GPXDataComputer;
import io.github.glandais.gpx.util.Vector;
import io.github.glandais.gpx.virtual.GPXEnhancer;
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
    private GPXFileReader gpxFileReader;

    @Autowired
    private GPXEnhancer gpxEnhancer;

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

    public Map parseAndStore(Team team, Map newMap, Path gpxFile, String defaultName) {

        try {
            GPX gpx = prepareMap(gpxFile, defaultName, defaultName != null, newMap, team);
            newMap.setName(gpx.name());
            return newMap;
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse GPX", e);
        }

    }

    public Map parseAndReplace(Team team, Map map, Path gpxFile) {
        try {
            prepareMap(gpxFile, map.getName(), true, map, team);
            return map;
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse GPX", e);
        }
    }

    public String parseAndStoreStandalone(Path gpxFile) {

        try {
            String uuid = UUID.randomUUID().toString();
            GPX gpx = parseGPX(gpxFile, null, false);

            return storeStandalone(gpx, uuid);
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse GPX", e);
        }

    }

    public String mergeAndStoreStandalone(Path... gpxFiles) {

        try {
            String uuid = UUID.randomUUID().toString();

            GPXPath path = new GPXPath(uuid, GPXPathType.TRACK);
            for (Path gpxFile : gpxFiles) {
                GPX gpx = parseGPX(gpxFile, uuid, true);
                for (GPXPath gpxPath : gpx.paths()) {
                    for (Point point : gpxPath.getPoints()) {
                        path.addPoint(point);
                    }
                }
            }

            return storeStandalone(new GPX(uuid, List.of(path), List.of()), uuid);
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse GPX", e);
        }

    }

    private String storeStandalone(GPX gpx, String uuid) {
        gpxEnhancer.virtualize(gpx, true);
        Path toStoreGpx = writeGpx(gpx);
        fileService.storeFile(toStoreGpx, FileRepositories.GPXTOOLVIEWER, uuid + ".gpx");
        return uuid;
    }

    public Optional<StandaloneGpx> getStandalone(String uuid) {
        if (fileService.fileExists(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx")) {

            Path file = fileService.getFile(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx");
            GPX gpx = parseGPX(file, null, false);

            return Optional.of(
                    new StandaloneGpx(
                            gpx.name(),
                            Rounder.round2Decimals(Math.round(10.0 * gpx.getDist()) / 10000.0),
                            Rounder.round1Decimal(gpx.getTotalElevation()),
                            Rounder.round1Decimal(gpx.getTotalElevationNegative())
                    )
            );

        }
        return Optional.empty();
    }

    private GPX prepareMap(final Path gpxFile, final String forcedName, final boolean erasePathNames,final Map map, final Team team) {
        GPX gpx = parseGPX(gpxFile, forcedName, erasePathNames);
        gpxEnhancer.virtualize(gpx, true);

        Path storedStaticMap = writeStaticMap(gpx);
        Path storedGpx = writeGpx(gpx);

        Vector windRaw = gpxDataComputer.getWind(gpx);
        info.tomacla.biketeam.common.geo.Vector wind = new info.tomacla.biketeam.common.geo.Vector(windRaw.x(), windRaw.y());
        boolean crossing = gpxDataComputer.isCrossing(gpx);

        List<GPXPath> paths = gpx.paths();
        Point startPoint = paths.get(0).getPoints().get(0);
        List<Point> points = paths.get(paths.size() - 1).getPoints();
        Point endPoint = points.get(points.size() - 1);

        info.tomacla.biketeam.common.geo.Point start = new info.tomacla.biketeam.common.geo.Point(startPoint.getLatDeg(), startPoint.getLonDeg());
        info.tomacla.biketeam.common.geo.Point end = new info.tomacla.biketeam.common.geo.Point(endPoint.getLatDeg(), endPoint.getLonDeg());

        map.setLength(Rounder.round2Decimals(Math.round(10.0 * gpx.getDist()) / 10000.0));
        map.setPostedAt(LocalDate.now(team.getZoneId()));
        map.setPositiveElevation(Rounder.round1Decimal(gpx.getTotalElevation()));
        map.setNegativeElevation(Rounder.round1Decimal(gpx.getTotalElevationNegative()));
        map.setStartPoint(start);
        map.setEndPoint(end);
        map.setWindDirection(WindDirection.findDirectionFromVector(wind));
        map.setCrossing(crossing);

        fileService.storeFile(storedGpx, FileRepositories.GPX_FILES, team.getId(), map.getId() + ".gpx");
        fileService.storeFile(storedStaticMap, FileRepositories.MAP_IMAGES, team.getId(), map.getId() + ".png");
        return gpx;
    }

    public void delete(Map map) {
        fileService.deleteFile(FileRepositories.GPX_FILES, map.getTeamId(), map.getId() + ".gpx");
        fileService.deleteFile(FileRepositories.MAP_IMAGES, map.getTeamId(), map.getId() + ".png");
    }

    public void refresh(Map map) {
        this.refresh(map, true, true);
    }

    public void refresh(Map map, boolean updateGpx, boolean updateImage) {

        GPX gpx = parseGPX(fileService.getFile(FileRepositories.GPX_FILES, map.getTeamId(), map.getId() + ".gpx"), map.getName(), false);

        if (updateGpx) {
            Path storedGpx = writeGpx(gpx);
            fileService.storeFile(storedGpx, FileRepositories.GPX_FILES, map.getTeamId(), map.getId() + ".gpx");
        }

        if (updateImage) {
            Path storedStaticMap = writeStaticMap(gpx);
            fileService.storeFile(storedStaticMap, FileRepositories.MAP_IMAGES, map.getTeamId(), map.getId() + ".png");
        }

    }

    public Path getAsFit(Path gpx, String forcedName) {
        try {
            Path fit = fileService.getTempFile("gpxsimplified", ".fit");
            fitFileWriter.writeGPX(parseGPX(gpx, forcedName, true), fit.toFile());
            return fit;
        } catch (Exception e) {
            log.error("Error while creating FIT", e);
            throw new RuntimeException(e);
        }
    }

    public Optional<MapData> getMapData(String uuid) {
        Optional<Path> gpxFile = getGpxFile(uuid);
        return gpxFile.map(path -> getMapData(path, uuid));
    }

    public Optional<Path> getGpxFile(String uuid) {
        if (fileService.fileExists(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx")) {
            Path file = fileService.getFile(FileRepositories.GPXTOOLVIEWER, uuid + ".gpx");
            return Optional.of(file);
        }
        return Optional.empty();
    }

    public MapData getMapData(Path gpxFile, String forcedName) {
        GPX gpx = parseGPX(gpxFile, forcedName, true);
        try {
            NavigableMap<Double, Integer> climbIndexes = new TreeMap<>();
            climbIndexes.put(0.0, null);
            NavigableMap<Double, Integer> climbPartIndexes = new TreeMap<>();
            climbPartIndexes.put(0.0, null);

            List<MapPoint> mapPoints = new ArrayList<>();
            List<Marker> markers = new ArrayList<>();
            List<Climb> allClimbs = new ArrayList<>();

            double dx = 0.0;
            int dclimb = 0;
            int di = 0;
            for (GPXPath gpxPath : gpx.paths()) {
                dclimb = dclimb + getMapDataForPath(gpxPath, dx, dclimb, di, mapPoints, markers, allClimbs, climbIndexes, climbPartIndexes);
                dx = dx + gpxPath.getDist();
                di = di + gpxPath.getPoints().size();
            }

            for (GPXWaypoint waypoint : gpx.waypoints()) {
                markers.add(new Marker(waypoint.point().getLatDeg(), waypoint.point().getLonDeg(), "waypoint", waypoint.name()));
            }

            return new MapData(getMapInfo(gpx), mapPoints, allClimbs, markers);
        } catch (Exception e) {
            log.error("Error while calculating Elevation profile", e);
            throw new RuntimeException(e);
        }
    }

    private int getMapDataForPath(GPXPath gpxPath, double dx, int dclimb, int di, List<MapPoint> mapPoints, List<Marker> markers, List<Climb> allClimbs, NavigableMap<Double, Integer> climbIndexes, NavigableMap<Double, Integer> climbPartIndexes) {
        List<Climb> climbs = climbDetector.getClimbs(gpxPath);

        for (int i = 0; i < climbs.size(); i++) {
            Climb climb = climbs.get(i);

            for (int j = 0; j < climb.parts().size(); j++) {
                climbPartIndexes.put(dx + climb.startDist() + climb.parts().get(j).startDist(), j);
            }
            climbPartIndexes.put(dx + climb.endDist(), null);

            climbIndexes.put(dx + climb.startDist(), i + dclimb);
            climbIndexes.put(dx + climb.endDist(), null);

            allClimbs.add(climb.shiftDist(dx));
        }

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

            mapPoints.add(new MapPoint(
                            di + i,
                            point.getLatDeg(),
                            point.getLonDeg(),
                        (dx + dist) / 1000.0,
                            point.getEle(),
                            point.getGrade() * 100,
                            climbIndexes.floorEntry(dx + dist).getValue(),
                            climbPartIndexes.floorEntry(dx + dist).getValue()
                    )
            );
        }

        return climbs.size();
    }

    private MapInfo getMapInfo(GPX gpx) {
        return new MapInfo(
                gpx.name(),
                Math.round(gpx.getDist() / 100.0) / 10.0,
                gpx.getTotalElevation(),
                gpx.getTotalElevationNegative()
        );
    }

    private GPX parseGPX(Path path, String forcedName, boolean erasePathNames) {
        try {
            return gpxFileReader.parseGPX(path.toFile(), forcedName, erasePathNames);
        } catch (Exception e) {
            log.error("Error while parsing GPX", e);
            throw new RuntimeException(e);
        }
    }

    private Path writeGpx(GPX gpx) {
        try {
            Path gpxFile = fileService.getTempFile("gpxsimplified", ".gpx");
            gpxFileWriter.writeGPX(gpx, gpxFile.toFile());
            return gpxFile;
        } catch (Exception e) {
            log.error("Error while creating FIT", e);
            throw new RuntimeException(e);
        }
    }

    private Path writeStaticMap(GPX gpx) {
        try {
            String tileUrl = "https://api.mapbox.com/styles/v1/mapbox/outdoors-v11/tiles/256/{z}/{x}/{y}?access_token=" + mapBoxAPIKey;
            Path staticMap = fileService.getTempFile("staticmap", ".png");
            tileMapProducer.createTileMap(staticMap.toFile(), gpx, tileUrl, 0, 768, 512);
            return staticMap;
        } catch (IOException e) {
            log.error("Error while getting static map", e);
            throw new RuntimeException(e);
        }
    }

}
