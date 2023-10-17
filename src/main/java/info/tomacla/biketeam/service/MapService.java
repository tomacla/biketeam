package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.domain.map.*;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.gpx.GpxService;
import info.tomacla.biketeam.service.permalink.AbstractPermalinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class MapService extends AbstractPermalinkService {

    private static final Logger log = LoggerFactory.getLogger(MapService.class);

    private final GpxService gpxService;

    private final FileService fileService;

    private final MapRepository mapRepository;

    @Autowired
    public MapService(GpxService gpxService, FileService fileService, MapRepository mapRepository) {
        this.gpxService = gpxService;
        this.fileService = fileService;
        this.mapRepository = mapRepository;
    }

    public Optional<Map> get(String teamId, String mapIdOrPermalink) {
        Optional<Map> optionalMap = mapRepository.findById(mapIdOrPermalink);
        if (optionalMap.isPresent() && optionalMap.get().getTeamId().equals(teamId)) {
            return optionalMap;
        }

        optionalMap = findByPermalink(mapIdOrPermalink);
        if (optionalMap.isPresent() && optionalMap.get().getTeamId().equals(teamId)) {
            return optionalMap;
        }

        return Optional.empty();

    }

    public void save(Map map) {
        this.save(map, false);
    }

    @Transactional
    public void save(Map map, boolean refreshFiles) {
        mapRepository.save(map);
        if (refreshFiles) {
            gpxService.refresh(map);
        }
    }

    public Map createFromGpx(Team team, InputStream gpxIs, String defaultName, String permalink) {
        log.info("Creating new map with default name {}", defaultName);
        if (!ObjectUtils.isEmpty(permalink)) {
            permalink = getPermalink(permalink);
        }
        final Map newMap = gpxService.parseAndStore(team, fileService.getTempFileFromInputStream(gpxIs), defaultName, permalink);
        return mapRepository.save(newMap);
    }

    public void replaceGpx(Team team, Map map, InputStream is) {
        log.info("Replacing GPX in map {}", map.getId());
        gpxService.parseAndReplace(team, map, fileService.getTempFileFromInputStream(is));
        mapRepository.save(map);
    }

    public Set<String> listTags(String teamId) {
        return this.listTags(teamId, null);
    }

    public Set<String> listTags(String teamId, String q) {
        if (ObjectUtils.isEmpty(q)) {
            return mapRepository.findAllDistinctTags(teamId);
        }
        return mapRepository.findDistinctTagsContaining(teamId, q.toLowerCase());
    }

    @Transactional
    public void delete(String teamId, String mapId) {
        log.info("Request map deletion {}", mapId);
        get(teamId, mapId).ifPresent(map -> {
            map.setDeletion(true);
            save(map);
        });
    }

    public Optional<Map> findByPermalink(String permalink) {
        return mapRepository.findOne(SearchMapSpecification.byPermalink(permalink));
    }

    @Override
    public boolean permalinkExists(String permalink) {
        return findByPermalink(permalink).isPresent();
    }

    public Page<Map> listMaps(String teamId, String name, int page, int pageSize) {
        return mapRepository.findAll(SearchMapSpecification.byNameInTeam(teamId, name), PageRequest.of(page, pageSize, getPageSort(null)));
    }

    public Page<Map> searchMaps(String teamId, String name, Double lowerDistance, Double upperDistance, MapType type,
                                Double lowerPositiveElevation, Double upperPositiveElevation,
                                List<String> tags, WindDirection windDirection,
                                int page, int pageSize, MapSorterOption sortOption) {

        Sort sort = getPageSort(sortOption);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        SearchMapSpecification spec = new SearchMapSpecification(
                false,
                null,
                teamId,
                name,
                lowerDistance,
                upperDistance,
                type,
                lowerPositiveElevation,
                upperPositiveElevation,
                tags,
                windDirection
        );

        return mapRepository.findAll(spec, pageable);

    }

    public Optional<Path> getFitFile(String teamId, String mapId) {
        Optional<Path> gpxFile = getGpxFile(teamId, mapId);
        if (gpxFile.isPresent()) {
            return Optional.of(gpxService.getAsFit(fileService.getFile(FileRepositories.GPX_FILES, teamId, getGpxName(mapId))));
        }
        return Optional.empty();
    }

    public Optional<Path> getGeoJsonFile(String teamId, String mapId) {
        Optional<Path> gpxFile = getGpxFile(teamId, mapId);
        if (gpxFile.isPresent()) {
            return Optional.of(gpxService.getAsGeoJson(fileService.getFile(FileRepositories.GPX_FILES, teamId, getGpxName(mapId))));
        }
        return Optional.empty();
    }

    public Optional<List<java.util.Map<String, Object>>> getElevationProfile(String teamId, String mapId) {
        Optional<Path> gpxFile = getGpxFile(teamId, mapId);
        if (gpxFile.isPresent()) {
            return Optional.of(gpxService.getElevationProfile(fileService.getFile(FileRepositories.GPX_FILES, teamId, getGpxName(mapId))));
        }
        return Optional.empty();
    }

    public Optional<Path> getGpxFile(String teamId, String mapId) {
        final Optional<Map> optionalMap = get(teamId, mapId);
        if (optionalMap.isPresent()) {
            String gpxName = getGpxName(mapId);
            if (fileService.fileExists(FileRepositories.GPX_FILES, teamId, gpxName)) {
                return Optional.of(fileService.getFile(FileRepositories.GPX_FILES, teamId, gpxName));
            }
        }
        return Optional.empty();
    }

    public Optional<Path> getImageFile(String teamId, String mapId) {
        final Optional<Map> optionalMap = get(teamId, mapId);
        if (optionalMap.isPresent()) {
            String mapImage = optionalMap.get().getId() + ".png";
            if (fileService.fileExists(FileRepositories.MAP_IMAGES, teamId, mapImage)) {
                return Optional.of(fileService.getFile(FileRepositories.MAP_IMAGES, teamId, mapImage));
            }
        }
        return Optional.empty();
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

    private static String getGpxName(Map map) {
        return getGpxName(map.getId());
    }

    private static String getGpxName(String mapId) {
        return mapId + ".gpx";
    }

}