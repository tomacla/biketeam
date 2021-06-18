package info.tomacla.biketeam.service;

import com.fasterxml.jackson.core.type.TypeReference;
import info.tomacla.biketeam.common.Json;
import info.tomacla.biketeam.domain.map.Map;
import info.tomacla.biketeam.domain.map.MapType;
import info.tomacla.biketeam.domain.team.Team;
import liquibase.util.file.FilenameUtils;
import net.lingala.zip4j.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ArchiveService {

    private static final Logger log = LoggerFactory.getLogger(ArchiveService.class);

    @Value("${archive.directory}")
    private String archiveDirectory;

    @Autowired
    private MapService mapService;

    @Autowired
    private TeamService teamService;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public List<String> listArchives() {
        return Stream.of(new File(archiveDirectory).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .filter(name -> name.endsWith(".zip"))
                .collect(Collectors.toList());
    }

    public void importArchive(String archiveName) {

        log.info("Request to import archive {}", archiveName);

        try {
            if (Files.exists(Path.of(archiveDirectory, archiveName))) {

                log.info("Perform archive {} import", archiveName);

                final Path targetArchive = Path.of(archiveDirectory, archiveName);

                // create target directory
                final String teamId = FilenameUtils.removeExtension(archiveName);
                Path unzipDestination = Path.of(archiveDirectory, teamId);
                Files.createDirectories(unzipDestination);

                // extract to directory
                new ZipFile(targetArchive.toFile()).extractAll(unzipDestination.toString());

                if (Files.exists(Path.of(unzipDestination.toString(), "descriptor.json"))) {
                    final List<ImportMapElement> importMapElements = Json.parse(Path.of(unzipDestination.toString(), "descriptor.json"), new TypeReference<>() {
                    });

                    for (ImportMapElement importMapElement : importMapElements) {
                        executor.submit(new ImportMap(teamId, unzipDestination, importMapElement, mapService, teamService));
                    }

                }

            }
        } catch (IOException e) {
            log.info("Error while importing archive " + archiveName, e);
            throw new RuntimeException(e);
        }
    }

    public static class ImportMapElement {

        private String permatitle;
        private String fileName;
        private String name;
        private MapType type;
        private boolean visible;
        private List<String> tags;
        private String date;

        public String getPermatitle() {
            return permatitle;
        }

        public void setPermatitle(String permatitle) {
            this.permatitle = permatitle;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public MapType getType() {
            return type;
        }

        public void setType(MapType type) {
            this.type = type;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }

    public static class ImportMap implements Runnable {

        private final String teamId;
        private final Path workingDirectory;
        private final ImportMapElement element;
        private final MapService mapService;
        private final TeamService teamService;

        public ImportMap(String teamId, Path workingDirectory, ImportMapElement element, MapService mapService, TeamService teamService) {
            this.teamId = teamId;
            this.workingDirectory = workingDirectory;
            this.element = element;
            this.mapService = mapService;
            this.teamService = teamService;
        }

        @Override
        public void run() {

            Optional<Team> optionalTeam = teamService.get(teamId);
            if (optionalTeam.isEmpty()) {
                log.warn("Import on empty team {}", teamId);
                return;
            }

            try {

                Team team = optionalTeam.get();

                log.info("Start import map {}", element.getFileName());

                final InputStream fileInputStream = Files.newInputStream(Path.of(workingDirectory.toString(), element.getFileName()));

                final String targetDate = Optional.ofNullable(element.getDate()).orElse(ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_DATE_TIME));

                if (element.getPermatitle() != null) {
                    mapService.delete(team.getId(), element.getPermatitle());
                }

                final Map newMap = mapService.save(team, fileInputStream, element.getName(), element.getPermatitle());

                newMap.setType(element.getType());
                newMap.setTags(element.getTags());
                newMap.setVisible(element.isVisible());
                newMap.setPostedAt(ZonedDateTime.parse(targetDate).toLocalDate());
                mapService.save(newMap);

                log.info("End import map {}", element.getFileName());

            } catch (Exception e) {
                log.error("Error while importing map " + element.getFileName(), e);
            }
        }

    }

}
