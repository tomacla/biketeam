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
import io.github.glandais.gpx.filter.GPXFilter;
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

    @Value("${mapbox.api-key}")
    private String mapBoxAPIKey;

    public Map parseAndStore(Team team, Path gpx, String defaultName, String permalink) {

        Map newMap = new Map();
        newMap.setTeamId(team.getId());
        newMap.setPermalink(permalink);
        newMap.setType(MapType.ROAD);
        newMap.setTags(team.getConfiguration().getDefaultSearchTags());

        GPXPath gpxPath = prepareMap(gpx, defaultName, newMap, team);

        newMap.setName(gpxPath.getName());

        return newMap;

    }

    public Map parseAndReplace(Team team, Map map, Path gpx) {

        prepareMap(gpx, map.getName(), map, team);
        return map;
    }

    private GPXPath prepareMap(final Path gpx, final String defaultName, final Map map, final Team team) {
        GPXPath gpxPath = getGPXPath(gpx, defaultName);

        if (team.getType().isSimplifyGpx()) {
            gpxPathEnhancer.virtualize(gpxPath);
            GPXFilter.filterPointsDouglasPeucker(gpxPath);
        }

        Path storedStaticMap = getStaticMap(gpxPath);
        Path storedFit = getFit(gpxPath);
        Path storedGpx = getGpx(gpxPath);
        Path storedGeoJson = getGeoJson(gpxPath);

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
        fileService.storeFile(storedFit, FileRepositories.FIT_FILES, team.getId(), map.getId() + ".fit");
        fileService.storeFile(storedGeoJson, FileRepositories.GEOJSON_FILES, team.getId(), map.getId() + ".json");
        fileService.storeFile(storedStaticMap, FileRepositories.MAP_IMAGES, team.getId(), map.getId() + ".png");
        return gpxPath;
    }

    public void delete(Map map) {
        fileService.deleteFile(FileRepositories.GPX_FILES, map.getTeamId(), map.getId() + ".gpx");
        fileService.deleteFile(FileRepositories.GEOJSON_FILES, map.getTeamId(), map.getId() + ".json");
        fileService.deleteFile(FileRepositories.FIT_FILES, map.getTeamId(), map.getId() + ".fit");
        fileService.deleteFile(FileRepositories.MAP_IMAGES, map.getTeamId(), map.getId() + ".png");
    }

    public void refresh(Map map) {
        this.refresh(map, true, true, true, true);
    }

    public void refresh(Map map, boolean gpx, boolean fit, boolean geoJson, boolean image) {

        GPXPath gpxPath = getGPXPath(fileService.getFile(FileRepositories.GPX_FILES, map.getTeamId(), map.getId() + ".gpx"), map.getName());

        if(gpx) {
            Path storedGpx = getGpx(gpxPath);
            fileService.storeFile(storedGpx, FileRepositories.GPX_FILES, map.getTeamId(), map.getId() + ".gpx");
        }

        if(fit) {
            Path storedFit = getFit(gpxPath);
            fileService.storeFile(storedFit, FileRepositories.FIT_FILES, map.getTeamId(), map.getId() + ".fit");
        }

        if(geoJson) {
            Path storedGeoJson = getGeoJson(gpxPath);
            fileService.storeFile(storedGeoJson, FileRepositories.GEOJSON_FILES, map.getTeamId(), map.getId() + ".json");
        }

        if(image) {
            Path storedStaticMap = getStaticMap(gpxPath);
            fileService.storeFile(storedStaticMap, FileRepositories.MAP_IMAGES, map.getTeamId(), map.getId() + ".png");
        }

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

    private Path getGeoJson(GPXPath gpxPath) {
        try {
            Path geoJson = fileService.getTempFile("gpxsimplified", ".json");
            geoJsonFileWriter.writeGeoJsonFile(gpxPath, geoJson.toFile());
            return geoJson;
        } catch (Exception e) {
            log.error("Error while creating GeoJSON", e);
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
