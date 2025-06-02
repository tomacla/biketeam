package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.geo.Point;
import info.tomacla.biketeam.domain.map.*;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.gpx.GpxService;
import info.tomacla.biketeam.service.gpx.MapData;
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
    
    private final MapRatingRepository mapRatingRepository;

    @Autowired
    public MapService(GpxService gpxService, FileService fileService, MapRepository mapRepository, 
                     MapRatingRepository mapRatingRepository) {
        this.gpxService = gpxService;
        this.fileService = fileService;
        this.mapRepository = mapRepository;
        this.mapRatingRepository = mapRatingRepository;
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

        Map newMap = new Map();
        newMap.setTeamId(team.getId());
        newMap.setPermalink(permalink);
        newMap.setType(MapType.ROAD);
        newMap.setTags(team.getConfiguration().getDefaultSearchTags());
        newMap.setName(defaultName);
        newMap = mapRepository.save(newMap);
        try {
            newMap = gpxService.parseAndStore(team, newMap, fileService.getTempFileFromInputStream(gpxIs), defaultName.endsWith(".gpx") ? null : defaultName);
        } catch(Exception e) {
            mapRepository.delete(newMap);
            throw e;
        }
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

    public Page<Map> searchMaps(Set<String> teamIds, String name, Double lowerDistance, Double upperDistance, MapType type,
                                Double lowerPositiveElevation, Double upperPositiveElevation,
                                List<String> tags, WindDirection windDirection, Point center, Integer distanceToCenter,
                                int page, int pageSize, MapSorterOption sortOption) {

        Sort sort = getPageSort(sortOption);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        SearchMapSpecification spec = new SearchMapSpecification(
                false,
                null,
                teamIds,
                name,
                lowerDistance,
                upperDistance,
                type,
                lowerPositiveElevation,
                upperPositiveElevation,
                tags,
                windDirection,
                center,
                distanceToCenter
        );

        return mapRepository.findAll(spec, pageable);

    }

    public Optional<Path> getFitFile(String teamId, String mapId) {
        Optional<Path> gpxFile = getGpxFile(teamId, mapId);
        Optional<Map> map = get(teamId, mapId);
        if (gpxFile.isPresent() && map.isPresent()) {
            return Optional.of(gpxService.getAsFit(gpxFile.get(), map.get().getName()));
        }
        return Optional.empty();
    }

    public Optional<MapData> getMapData(String teamId, String mapId) {
        Optional<Path> gpxFile = getGpxFile(teamId, mapId);
        Optional<Map> map = get(teamId, mapId);
        if (gpxFile.isPresent() && map.isPresent()) {
            return Optional.of(gpxService.getMapData(gpxFile.get(), map.get().getName()));
        }
        return Optional.empty();
    }

    public Optional<Path> getGpxFile(String teamId, String mapId) {
        final Optional<Map> optionalMap = get(teamId, mapId);
        if (optionalMap.isPresent()) {
            String gpxName = getGpxName(optionalMap.get());
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
            } else if (sortOption.equals(MapSorterOption.BEST_RATED)) {
                sort = Sort.by("averageRating").descending().and(Sort.by("ratingCount").descending());
            } else if (sortOption.equals(MapSorterOption.WORST_RATED)) {
                sort = Sort.by("averageRating").ascending().and(Sort.by("ratingCount").ascending());
            }
        }
        return sort;
    }

    // Rating methods
    @Transactional
    public Integer rateMap(String mapId, User user, int rating) {
        Optional<MapRating> existingRating = mapRatingRepository.findByMapIdAndUserId(mapId, user.getId());

        Integer newRating;
        if (existingRating.isPresent()) {
            MapRating mapRating = existingRating.get();
            if (mapRating.getRating() != rating) {
                mapRating.setRating(rating);
                mapRatingRepository.save(mapRating);
                newRating = rating;
            } else {
                mapRatingRepository.deleteById(mapRating.getId());
                newRating = null;
            }
        } else {
            Map map = mapRepository.findById(mapId)
                .orElseThrow(() -> new IllegalArgumentException("Map not found"));
            MapRating mapRating = new MapRating(map, user, rating);
            mapRatingRepository.save(mapRating);
            newRating = rating;
        }
        
        // Update cached rating values in map
        updateCachedRatings(mapId);
        return newRating;
    }

    public Optional<MapRating> getUserRating(String mapId, String userId) {
        return mapRatingRepository.findByMapIdAndUserId(mapId, userId);
    }

    public Double getAverageRating(String mapId) {
        return mapRepository.findById(mapId)
                .map(Map::getAverageRating)
                .orElse(null);
    }

    public Long getRatingCount(String mapId) {
        return mapRepository.findById(mapId)
                .map(map -> map.getRatingCount().longValue())
                .orElse(0L);
    }

    private void updateCachedRatings(String mapId) {
        Map map = mapRepository.findById(mapId)
                .orElseThrow(() -> new IllegalArgumentException("Map not found"));
        
        // Get fresh rating data from repository
        Double averageRating = mapRatingRepository.findAverageRatingByMapId(mapId);
        Long ratingCount = mapRatingRepository.countRatingsByMapId(mapId);
        
        // Update cached values
        map.setAverageRating(averageRating != null ? averageRating : 0.0);
        map.setRatingCount(ratingCount != null ? ratingCount.intValue() : 0);
        
        mapRepository.save(map);
    }

    private static String getGpxName(Map map) {
        return map.getId() + ".gpx";
    }

}