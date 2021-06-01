package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.Point;
import info.tomacla.biketeam.common.Vector;
import info.tomacla.biketeam.domain.map.*;
import io.github.glandais.GPXDataComputer;
import io.github.glandais.GPXPathEnhancer;
import io.github.glandais.fit.FitFileWriter;
import io.github.glandais.gpx.GPXFilter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.map.TileMapImage;
import io.github.glandais.map.TileMapProducer;
import io.github.glandais.srtm.GPXElevationFixer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MapService {

    @Autowired
    private FileService fileService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private MapRepository mapRepository;

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

    public void delete(String mapId) {
        get(mapId).ifPresent(map -> mapRepository.delete(map));
    }

    public void save(Map map) {
        mapRepository.save(map);
    }

    public Map save(InputStream is, String defaultName) {

        Path gpx = fileService.getTempFileFromInputStream(is);

        GPXPath gpxPath = getGPXPath(gpx, defaultName);

        gpxPathEnhancer.virtualize(gpxPath);
        GPXFilter.filterPointsDouglasPeucker(gpxPath);

        io.github.glandais.util.Vector windRaw = gpxDataComputer.getWind(gpxPath);
        Vector wind = new Vector(windRaw.getX(), windRaw.getY());
        boolean crossing = gpxDataComputer.isCrossing(gpxPath);

        Path staticMap = getStaticMap(gpxPath);

        Path fit = getFit(gpxPath);

        List<io.github.glandais.gpx.Point> points = gpxPath.getPoints();
        io.github.glandais.gpx.Point startPoint = points.get(0);
        io.github.glandais.gpx.Point endPoint = points.get(points.size() - 1);

        Point start = new Point(startPoint.getLatDeg(), startPoint.getLonDeg());
        Point end = new Point(endPoint.getLatDeg(), endPoint.getLonDeg());

        Map newMap = new Map(
                gpxPath.getName(),
                Math.round(10.0 * gpxPath.getDist()) / 10000.0f,
                MapType.ROAD,
                (int) Math.round(gpxPath.getTotalElevation()),
                (int) Math.round(gpxPath.getTotalElevationNegative()),
                new ArrayList<>(),
                start,
                end,
                WindDirection.findDirectionFromVector(wind),
                crossing,
                false
        );

        fileService.store(gpx, FileRepositories.GPX_FILES, newMap.getId() + ".gpx");
        fileService.store(fit, FileRepositories.FIT_FILES, newMap.getId() + ".fit");
        fileService.store(staticMap, FileRepositories.MAP_IMAGES, newMap.getId() + ".png");

        return mapRepository.save(newMap);

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
            throw new RuntimeException(e);
        }
        return gpxPath;
    }

    private Path getFit(GPXPath gpxPath) {
        try {
            Path fit = Files.createTempFile("gpx-simplified", ".fit");
            fitFileWriter.writeFitFile(gpxPath, fit.toFile());
            return fit;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getStaticMap(GPXPath path) {
        try {
            String mapBoxAPIKey = configurationService.getSiteIntegration().getMapBoxAPIKey();
            String tileUrl = mapBoxAPIKey != null ? "https://api.mapbox.com/styles/v1/mapbox/outdoors-v11/tiles/256/{z}/{x}/{y}?access_token=" + mapBoxAPIKey : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";

            TileMapImage tileMap = tileMapProducer.createTileMap(path, tileUrl, 0, 768, 512);
            Path staticMap = Files.createTempFile("staticmap", ".png");
            ImageIO.write(tileMap.getImage(), "png", staticMap.toFile());
            return staticMap;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Map> get(String mapId) {
        return mapRepository.findById(mapId);
    }

    public List<String> listTags() {
        return this.listTags(null);
    }

    public List<String> listTags(String q) {
        if (q == null || q.isBlank()) {
            return mapRepository.findAllDistinctTags();
        }
        return mapRepository.findDistinctTagsContainer(q.toLowerCase());
    }

    public List<MapIdNamePostedAtVisibleProjection> searchMaps(String q) {
        return (q == null || q.isBlank()) ? mapRepository.findAllByOrderByPostedAtDesc()
                : mapRepository.findAllByNameContainingIgnoreCaseOrderByPostedAtDesc(q);
    }

    public List<MapIdNamePostedAtVisibleProjection> listMaps() {
        return mapRepository.findAllByOrderByPostedAtDesc();
    }

    public Page<Map> listMaps(int pageSize) {
        return this.listMaps(0, pageSize);
    }

    public Page<Map> listMaps(int page, int pageSize) {
        return this.listMaps(page, pageSize, null);
    }

    public Page<Map> listMaps(int page, int pageSize, MapSorterOption sortOption) {
        return mapRepository.findAll(PageRequest.of(page, pageSize, getPageSort(sortOption)));
    }

    public Page<Map> searchMaps(int page, int pageSize, MapSorterOption sortOption,
                                double lowerDistance, double upperDistance, List<String> tags, WindDirection windDirection) {
        Sort sort = getPageSort(sortOption);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        SearchMapSpecification spec = new SearchMapSpecification(lowerDistance, upperDistance, tags, windDirection);

        return mapRepository.findAll(spec, pageable);

    }

    public Optional<Path> getFitFile(String mapId) {
        String fitName = mapId + ".fit";
        if (fileService.exists(FileRepositories.FIT_FILES, fitName)) {
            return Optional.of(fileService.get(FileRepositories.FIT_FILES, fitName));
        }
        return Optional.empty();
    }

    public Optional<Path> getGpxFile(String mapId) {
        String gpxName = mapId + ".gpx";
        if (fileService.exists(FileRepositories.GPX_FILES, gpxName)) {
            return Optional.of(fileService.get(FileRepositories.GPX_FILES, gpxName));
        }
        return Optional.empty();
    }

    public Optional<Path> getImageFile(String mapId) {
        String mapImage = mapId + ".png";
        if (fileService.exists(FileRepositories.MAP_IMAGES, mapImage)) {
            return Optional.of(fileService.get(FileRepositories.MAP_IMAGES, mapImage));
        }
        return Optional.empty();
    }

    public void generateImage(String mapId) {
        final Optional<Path> gpxFile = this.getGpxFile(mapId);
        if (gpxFile.isPresent()) {
            GPXPath gpxPath = getGPXPath(gpxFile.get(), "");
            Path staticMapImage = getStaticMap(gpxPath);
            fileService.store(staticMapImage, FileRepositories.MAP_IMAGES, mapId + ".png");
        }
    }

    private Sort getPageSort(MapSorterOption sortOption) {
        Sort sort = Sort.by("postedAt").descending();
        if (sortOption != null) {
            if (sortOption.equals(MapSorterOption.SHORT)) {
                sort = Sort.by("length").ascending();
            } else if (sortOption.equals(MapSorterOption.LONG)) {
                sort = Sort.by("length").descending();
            } else if (sortOption.equals(MapSorterOption.HILLY)) {
                sort = Sort.by("positiveElevation").descending();
            } else if (sortOption.equals(MapSorterOption.FLAT)) {
                sort = Sort.by("positiveElevation").ascending();
            }
        }
        return sort;
    }


}
