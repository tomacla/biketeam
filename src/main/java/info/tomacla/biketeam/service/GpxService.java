package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.Rounder;
import info.tomacla.biketeam.common.Strings;
import info.tomacla.biketeam.common.Vector;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.map.WindDirection;
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
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class GpxService {

    private static final Logger log = LoggerFactory.getLogger(GpxService.class);

    @Autowired
    private ConfigurationService configurationService;

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

    public Map parseAndStore(Path gpx, String defaultName, String forceId) {

        GPXPath gpxPath = getGPXPath(gpx, defaultName);

        gpxPathEnhancer.virtualize(gpxPath);
        GPXFilter.filterPointsDouglasPeucker(gpxPath);

        io.github.glandais.util.Vector windRaw = gpxDataComputer.getWind(gpxPath);
        Vector wind = new Vector(windRaw.getX(), windRaw.getY());
        boolean crossing = gpxDataComputer.isCrossing(gpxPath);

        Path staticMap = getStaticMap(gpxPath);

        Path fit = getFit(gpxPath);

        List<Point> points = gpxPath.getPoints();
        io.github.glandais.gpx.Point startPoint = points.get(0);
        io.github.glandais.gpx.Point endPoint = points.get(points.size() - 1);

        info.tomacla.biketeam.common.Point start = new info.tomacla.biketeam.common.Point(startPoint.getLatDeg(), startPoint.getLonDeg());
        info.tomacla.biketeam.common.Point end = new info.tomacla.biketeam.common.Point(endPoint.getLatDeg(), endPoint.getLonDeg());

        Map newMap = new Map(
                Strings.permatitleFromString(gpxPath.getName()),
                gpxPath.getName(),
                Rounder.round2Decimals(Math.round(10.0 * gpxPath.getDist()) / 10000.0),
                MapType.ROAD,
                Rounder.round1Decimal(gpxPath.getTotalElevation()),
                Rounder.round1Decimal(gpxPath.getTotalElevationNegative()),
                new ArrayList<>(),
                start,
                end,
                WindDirection.findDirectionFromVector(wind),
                crossing,
                false
        );

        if (forceId != null) {
            newMap.setId(forceId);
        }

        fileService.store(gpx, FileRepositories.GPX_FILES, newMap.getId() + ".gpx");
        fileService.store(fit, FileRepositories.FIT_FILES, newMap.getId() + ".fit");
        fileService.store(staticMap, FileRepositories.MAP_IMAGES, newMap.getId() + ".png");

        return newMap;

    }

    public void generateImage(String mapId, Path gpxFile) {
        GPXPath gpxPath = getGPXPath(gpxFile, "");
        Path staticMapImage = getStaticMap(gpxPath);
        fileService.store(staticMapImage, FileRepositories.MAP_IMAGES, mapId + ".png");
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

    private Path getStaticMap(GPXPath path) {
        try {
            String mapBoxAPIKey = configurationService.getSiteIntegration().getMapBoxAPIKey();
            String tileUrl = mapBoxAPIKey != null ? "https://api.mapbox.com/styles/v1/mapbox/outdoors-v11/tiles/256/{z}/{x}/{y}?access_token=" + mapBoxAPIKey : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";

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
