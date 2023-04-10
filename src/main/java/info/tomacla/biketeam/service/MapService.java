package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.data.PublishedStatus;
import info.tomacla.biketeam.common.data.Timezone;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.domain.feed.FeedOptions;
import info.tomacla.biketeam.domain.map.*;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.ride.Ride;
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
import java.time.*;
import java.util.*;

@Service
public class MapService extends AbstractPermalinkService {

    private static final Logger log = LoggerFactory.getLogger(MapService.class);

    private final GpxService gpxService;

    private final FileService fileService;

    private final RideService rideService;

    private final TeamService teamService;

    private final MapRepository mapRepository;

    @Autowired
    public MapService(GpxService gpxService, FileService fileService, RideService rideService, TeamService teamService, MapRepository mapRepository) {
        this.gpxService = gpxService;
        this.fileService = fileService;
        this.rideService = rideService;
        this.teamService = teamService;
        this.mapRepository = mapRepository;
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

    public Map createFromGpx(Team team, InputStream is, String defaultName, String permalink) {
        log.info("Creating new map with default name {}", defaultName);
        if (!ObjectUtils.isEmpty(permalink)) {
            permalink = getPermalink(permalink);
        }
        final Map newMap = gpxService.parseAndStore(team, fileService.getTempFileFromInputStream(is), defaultName, permalink);
        return mapRepository.save(newMap);
    }

    public void replaceGpx(Team team, Map map, InputStream is) {
        log.info("Replacing GPX in map {}", map.getId());
        gpxService.parseAndReplace(team, map, fileService.getTempFileFromInputStream(is));
        mapRepository.save(map);
    }

    public Optional<Map> get(String teamId, String mapId) {
        Optional<Map> optionalMap = mapRepository.findById(mapId);
        if (optionalMap.isPresent() && optionalMap.get().getTeamId().equals(teamId)) {
            return optionalMap;
        }

        optionalMap = mapRepository.findByPermalink(mapId);
        if (optionalMap.isPresent() && optionalMap.get().getTeamId().equals(teamId)) {
            return optionalMap;
        }

        return Optional.empty();

    }

    public List<String> listTags(String teamId) {
        return this.listTags(teamId, null);
    }

    public List<String> listTags(String teamId, String q) {
        if (ObjectUtils.isEmpty(q)) {
            return mapRepository.findAllDistinctTags(teamId);
        }
        return mapRepository.findDistinctTagsContaining(teamId, q.toLowerCase());
    }

    public void delete(String teamId, String mapId) {
        get(teamId, mapId).ifPresent(this::delete);
    }

    @Transactional
    public void delete(Map map) {
        log.info("Request map deletion {}", map.getId());
        mapRepository.removeMapIdInGroups(map.getId());
        mapRepository.removeMapIdInStages(map.getId());
        gpxService.delete(map);
        mapRepository.delete(map);
        log.info("Map deleted {}", map.getId());
    }

    public boolean permalinkExists(String permalink) {
        return mapRepository.findByPermalink(permalink).isPresent();
    }

    public List<MapProjection> listMaps(String teamId) {
        return mapRepository.findAllByTeamIdOrderByPostedAtDesc(teamId);
    }

    public Page<Map> listMaps(String teamId, int pageSize) {
        return this.listMaps(teamId, 0, pageSize);
    }

    public Page<Map> listMaps(String teamId, int page, int pageSize) {
        return this.listMaps(teamId, page, pageSize, null);
    }

    public Page<Map> listMaps(String teamId, int page, int pageSize, MapSorterOption sortOption) {
        return mapRepository.findByTeamId(teamId, PageRequest.of(page, pageSize, getPageSort(sortOption)));
    }

    public List<MapProjection> searchMaps(String teamId, String q) {
        return (q == null || q.isBlank()) ? mapRepository.findAllByTeamIdOrderByPostedAtDesc(teamId)
                : mapRepository.findAllByTeamIdAndNameContainingIgnoreCaseOrderByPostedAtDesc(teamId, q);
    }

    public Page<Map> searchMaps(String teamId, int page, int pageSize, MapSorterOption sortOption,
                                String name, double lowerDistance, double upperDistance, MapType type,
                                double lowerPositiveElevation, double upperPositiveElevation,
                                List<String> tags, WindDirection windDirection) {

        Sort sort = getPageSort(sortOption);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        SearchMapSpecification spec = new SearchMapSpecification(
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
        final Optional<Map> optionalMap = get(teamId, mapId);
        if (optionalMap.isPresent()) {
            String gpxName = optionalMap.get().getId() + ".gpx";
            if (fileService.fileExists(FileRepositories.GPX_FILES, teamId, gpxName)) {
                return Optional.of(gpxService.getAsFit(fileService.getFile(FileRepositories.GPX_FILES, teamId, gpxName)));
            }
        }
        return Optional.empty();
    }

    public Optional<Path> getGeoJsonFile(String teamId, String mapId) {
        final Optional<Map> optionalMap = get(teamId, mapId);
        if (optionalMap.isPresent()) {
            String gpxName = optionalMap.get().getId() + ".gpx";
            if (fileService.fileExists(FileRepositories.GPX_FILES, teamId, gpxName)) {
                return Optional.of(gpxService.getAsGeoJson(fileService.getFile(FileRepositories.GPX_FILES, teamId, gpxName)));
            }
        }
        return Optional.empty();
    }

    public Optional<List<java.util.Map<String, Object>>> getElevationProfile(String teamId, String mapId) {
        final Optional<Map> optionalMap = get(teamId, mapId);
        if (optionalMap.isPresent()) {
            String gpxName = optionalMap.get().getId() + ".gpx";
            if (fileService.fileExists(FileRepositories.GPX_FILES, teamId, gpxName)) {
                return Optional.of(gpxService.getElevationProfile(fileService.getFile(FileRepositories.GPX_FILES, teamId, gpxName)));
            }
        }
        return Optional.empty();
    }

    public Optional<Path> getGpxFile(String teamId, String mapId) {
        final Optional<Map> optionalMap = get(teamId, mapId);
        if (optionalMap.isPresent()) {
            String gpxName = optionalMap.get().getId() + ".gpx";
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

    public void deleteByTeam(String teamId) {
        mapRepository.findAllByTeamIdOrderByPostedAtDesc(teamId).stream().map(MapProjection::getId).forEach(mapRepository::deleteById);
    }

    record MapStart(Instant start, Map map) {
    }

    public List<Map> listMapsForNearestRides(String teamId) {
        // retrieve team zone id
        ZoneId zoneId = teamService.get(teamId).map(Team::getZoneId).orElse(ZoneId.of(Timezone.DEFAULT_TIMEZONE));
        // get rides
        FeedOptions options = new FeedOptions();
        List<Ride> rides = rideService.searchRides(Set.of(teamId), 0, 10, options.getFrom(), options.getTo()).toList();
        // now
        Instant now = Instant.now();

        // compare for nearest start
        Comparator<MapStart> nearesetComparator = Comparator.comparing(ms -> Duration.between(ms.start(), now).abs());
        Comparator<MapStart> idComparator = Comparator.comparing(ms -> ms.map().getId());
        // then compare by map id if duration is the same
        Comparator<MapStart> comparator = nearesetComparator.thenComparing(idComparator);

        List<Map> maps = rides.stream()
                // only published rides
                .filter(ride -> ride.getPublishedStatus() == PublishedStatus.PUBLISHED)
                // retrieve maps with start
                .flatMap(ride ->
                        ride.getGroups().stream()
                                // only groups with a map
                                .filter(rideGroup -> rideGroup.getMap() != null)
                                .map(rideGroup -> new MapStart(
                                        // ride start as instant
                                        ride.getDate().atTime(rideGroup.getMeetingTime()).atZone(zoneId).toInstant(),
                                        rideGroup.getMap()
                                ))
                )
                // sort by nearest starts
                .sorted(comparator)
                .map(MapStart::map)
                .toList();
        if (maps.isEmpty()) {
            // no rides planned
            return listMaps(teamId, 50).toList();
        }
        return maps;
    }

}