package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.common.Gpx;
import info.tomacla.biketeam.common.Vector;
import info.tomacla.biketeam.domain.map.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.InputStream;
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

    public void delete(String mapId) {
        get(mapId).ifPresent(map -> mapRepository.delete(map));
    }

    public void save(Map map) {
        mapRepository.save(map);
    }

    public Map save(InputStream is, String defaultName) {

        Path gpx = fileService.getTempFileFromInputStream(is);

        try {
            gpx = Gpx.simplify(gpx, defaultName);
        } catch (Exception e) {
            // ignore : if simplified fails, just use the original file
        }

        Gpx.GpxDescriptor gpxParsed = Gpx.parse(gpx, defaultName);
        Path staticMapImage = Gpx.staticImage(gpx, configurationService.getSiteIntegration().getMapBoxAPIKey());
        Path fit = Gpx.fit(gpx, defaultName);

        Vector windVector = gpxParsed.getWind();

        Map newMap = new Map(
                gpxParsed.getName(),
                gpxParsed.getLength(),
                MapType.ROAD,
                gpxParsed.getPositiveElevation(),
                gpxParsed.getNegativeElevation(),
                new ArrayList<>(),
                gpxParsed.getStart(),
                gpxParsed.getEnd(),
                WindDirection.findDirectionFromVector(windVector),
                gpxParsed.isCrossing(),
                false
        );

        fileService.store(gpx, FileRepositories.GPX_FILES, newMap.getId() + ".gpx");
        fileService.store(fit, FileRepositories.FIT_FILES, newMap.getId() + ".fit");
        fileService.store(staticMapImage, FileRepositories.MAP_IMAGES, newMap.getId() + ".png");

        return mapRepository.save(newMap);

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
            Path staticMapImage = Gpx.staticImage(gpxFile.get(), configurationService.getSiteIntegration().getMapBoxAPIKey());
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
