package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.Rounder;
import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.common.Vector;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.map.WindDirection;
import info.tomacla.biketeam.domain.team.Team;
import io.github.glandais.GPXDataComputer;
import io.github.glandais.GPXPathEnhancer;
import io.github.glandais.fit.FitFileWriter;
import io.github.glandais.gpx.GPXFilter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.map.TileMapImage;
import io.github.glandais.map.TileMapProducer;
import io.github.glandais.srtm.GPXElevationFixer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
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
    private GPXElevationFixer gpxElevationFixer;

    @Autowired
    private FitFileWriter fitFileWriter;

    @Autowired
    private TileMapProducer tileMapProducer;

    @Value("${mapbox.api-key}")
    private String mapBoxAPIKey;

    public Map parseAndReplace(Team team, Map map, Path gpx) {

        GPXPath gpxPath = getGPXPath(gpx, map.getName());
        gpxPathEnhancer.virtualize(gpxPath);
        GPXFilter.filterPointsDouglasPeucker(gpxPath);

        Path storedStaticMap = getStaticMap(gpxPath);
        Path storedFit = getFit(gpxPath);
        Path storedGpx = getGpx(gpxPath);

        io.github.glandais.util.Vector windRaw = gpxDataComputer.getWind(gpxPath);
        Vector wind = new Vector(windRaw.getX(), windRaw.getY());
        boolean crossing = gpxDataComputer.isCrossing(gpxPath);

        List<Point> points = gpxPath.getPoints();
        io.github.glandais.gpx.Point startPoint = points.get(0);
        io.github.glandais.gpx.Point endPoint = points.get(points.size() - 1);

        info.tomacla.biketeam.common.Point start = new info.tomacla.biketeam.common.Point(startPoint.getLatDeg(), startPoint.getLonDeg());
        info.tomacla.biketeam.common.Point end = new info.tomacla.biketeam.common.Point(endPoint.getLatDeg(), endPoint.getLonDeg());

        map.setLength(Rounder.round2Decimals(Math.round(10.0 * gpxPath.getDist()) / 10000.0));
        map.setPostedAt(LocalDate.now(team.getZoneId()));
        map.setPositiveElevation(Rounder.round1Decimal(gpxPath.getTotalElevation()));
        map.setNegativeElevation(Rounder.round1Decimal(gpxPath.getTotalElevationNegative()));
        map.setStartPoint(start);
        map.setEndPoint(end);
        map.setWindDirection(WindDirection.findDirectionFromVector(wind));
        map.setCrossing(crossing);

        fileService.store(storedGpx, FileRepositories.GPX_FILES, team.getId(), map.getId() + ".gpx");
        fileService.store(storedFit, FileRepositories.FIT_FILES, team.getId(), map.getId() + ".fit");
        fileService.store(storedStaticMap, FileRepositories.MAP_IMAGES, team.getId(), map.getId() + ".png");

        return map;

    }

    public Map parseAndStore(Team team, Path gpx, String defaultName, String forceId) {

        GPXPath gpxPath = getGPXPath(gpx, defaultName);
        gpxPathEnhancer.virtualize(gpxPath);
        GPXFilter.filterPointsDouglasPeucker(gpxPath);

        Path storedStaticMap = getStaticMap(gpxPath);
        Path storedFit = getFit(gpxPath);
        Path storedGpx = getGpx(gpxPath);

        io.github.glandais.util.Vector windRaw = gpxDataComputer.getWind(gpxPath);
        Vector wind = new Vector(windRaw.getX(), windRaw.getY());
        boolean crossing = gpxDataComputer.isCrossing(gpxPath);

        List<Point> points = gpxPath.getPoints();
        io.github.glandais.gpx.Point startPoint = points.get(0);
        io.github.glandais.gpx.Point endPoint = points.get(points.size() - 1);

        info.tomacla.biketeam.common.Point start = new info.tomacla.biketeam.common.Point(startPoint.getLatDeg(), startPoint.getLonDeg());
        info.tomacla.biketeam.common.Point end = new info.tomacla.biketeam.common.Point(endPoint.getLatDeg(), endPoint.getLonDeg());

        Map newMap = new Map(
                forceId != null ? forceId : Strings.permatitleFromString(gpxPath.getName()),
                team.getId(),
                gpxPath.getName(),
                Rounder.round2Decimals(Math.round(10.0 * gpxPath.getDist()) / 10000.0),
                MapType.ROAD,
                LocalDate.now(team.getZoneId()),
                Rounder.round1Decimal(gpxPath.getTotalElevation()),
                Rounder.round1Decimal(gpxPath.getTotalElevationNegative()),
                team.getConfiguration().getDefaultSearchTags(),
                start,
                end,
                WindDirection.findDirectionFromVector(wind),
                crossing,
                true
        );

        fileService.store(storedGpx, FileRepositories.GPX_FILES, team.getId(), newMap.getId() + ".gpx");
        fileService.store(storedFit, FileRepositories.FIT_FILES, team.getId(), newMap.getId() + ".fit");
        fileService.store(storedStaticMap, FileRepositories.MAP_IMAGES, team.getId(), newMap.getId() + ".png");

        return newMap;

    }

    public void deleteMap(String teamId, String mapId) {
        fileService.delete(FileRepositories.GPX_FILES, teamId, mapId + ".gpx");
        fileService.delete(FileRepositories.FIT_FILES, teamId, mapId + ".fit");
        fileService.delete(FileRepositories.MAP_IMAGES, teamId, mapId + ".png");
    }

    public void changeMapId(String teamId, String oldId, String newId) {
        fileService.move(FileRepositories.GPX_FILES, teamId, oldId + ".gpx", newId + ".gpx");
        fileService.move(FileRepositories.FIT_FILES, teamId, oldId + ".fit", newId + ".fit");
        fileService.move(FileRepositories.MAP_IMAGES, teamId, oldId + ".png", newId + ".png");
    }

    public void renameMap(String teamId, String mapId, String newName) {

        GPXPath gpxPath = getGPXPath(fileService.get(FileRepositories.GPX_FILES, teamId, mapId + ".gpx"), newName);
        Path storedStaticMap = getStaticMap(gpxPath);
        Path storedFit = getFit(gpxPath);
        Path storedGpx = getGpx(gpxPath);

        fileService.store(storedGpx, FileRepositories.GPX_FILES, teamId, mapId + ".gpx");
        fileService.store(storedFit, FileRepositories.FIT_FILES, teamId, mapId + ".fit");
        fileService.store(storedStaticMap, FileRepositories.MAP_IMAGES, teamId, mapId + ".png");
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

    private Path getFit(GPXPath gpxPath) {
        try {
            Path fit = fileService.getTempFile("gpxsimplified", ".fit");
            fitFileWriter.writeFitFile(gpxPath, fit.toFile());
            return fit;
        } catch (Exception e) {
            log.error("Error while creating FIT", e);
            throw new RuntimeException(e);
        }
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
