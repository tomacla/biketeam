package info.tomacla.biketeam.service.heatmap;

import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.common.geo.Point;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.amqp.dto.TeamDTO;
import info.tomacla.biketeam.service.file.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class HeatmapService {

    @Value("${heatmap.path:undefined}")
    private String heatmapPath;

    @Autowired
    private FileService fileService;

    @Autowired
    private TeamService teamService;

    private static final Logger log = LoggerFactory.getLogger(HeatmapService.class);
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    public Optional<ImageDescriptor> get(String teamId) {
        if (fileService.fileExists(FileRepositories.MISC_IMAGES, teamId, "heatmap.png")) {
            final Path path = fileService.getFile(FileRepositories.MISC_IMAGES, teamId, "heatmap.png");
            return Optional.of(ImageDescriptor.of(FileExtension.PNG, path));
        }
        return Optional.empty();
    }

    @RabbitListener(queues = Queues.TASK_GENERATE_HEATMAPS)
    public void generateAll() {
        teamService.list().forEach(this::performGenerateHeatmap);
    }

    @RabbitListener(queues = Queues.TASK_GENERATE_HEATMAP)
    public void generateHeatmap(TeamDTO team) {
        if (isConfigured() && team != null) {
            teamService.get(team.teamId).ifPresent(t -> performGenerateHeatmap(t));
        }
    }

    private void performGenerateHeatmap(Team team) {
        if (team.getIntegration().isHeatmapConfigured()) {
            log.info("Generating heatmap for team {}", team.getId());
            final Path gpxPath = fileService.getDirectory(FileRepositories.GPX_FILES, team.getId());
            final Path outputPath = fileService.getFile(FileRepositories.MISC_IMAGES, team.getId(), "heatmap.png");
            executor.submit(new GenerateHeatmap(heatmapPath, team.getId(), team.getIntegration().getHeatmapCenter(),
                    gpxPath.toAbsolutePath().toString(),
                    outputPath.toAbsolutePath().toString()));
        }
    }


    public static class GenerateHeatmap implements Runnable {

        private final String heatmapPath;
        private final String teamId;
        private final Point center;
        private final String gpxPath;
        private final String outputPath;

        public GenerateHeatmap(String heatmapPath, String teamId, Point center, String gpxPath, String outputPath) {
            this.heatmapPath = heatmapPath;
            this.teamId = teamId;
            this.center = center;
            this.gpxPath = gpxPath;
            this.outputPath = outputPath;
        }

        @Override
        public void run() {

            try {

                ProcessBuilder pb = new ProcessBuilder("python3.9", "heatmap.py", "--dir", gpxPath, "--output", outputPath,
                        "--bounds", String.valueOf(center.getLat() - 0.2), String.valueOf(center.getLat() + 0.2),
                        String.valueOf(center.getLng() - 0.5), String.valueOf(center.getLng() + 0.5));
                pb.directory(new File(heatmapPath));

                Process p = pb.start();

                if (!p.waitFor(30, TimeUnit.SECONDS)) {
                    p.destroy();
                    throw new RuntimeException("Unexpected error : heatmap.py not finished");
                }

                int exitStatus = p.exitValue();

                if (exitStatus != 0) {
                    throw new RuntimeException("Unexpected error : heatmap.py finished with status " + exitStatus);
                }

                Scanner s = new Scanner(p.getErrorStream()).useDelimiter("\\A");
                String errorStream = s.hasNext() ? s.next() : "";
                if (!ObjectUtils.isEmpty(errorStream)) {
                    throw new RuntimeException("Unexpected error : heatmap.py output some errors " + errorStream);
                }

            } catch (Exception e) {
                log.error("Unable to generate heatmap " + teamId, e);
                throw new RuntimeException("Unable to generate heatmap " + teamId);
            }

        }

    }

    private boolean isConfigured() {
        return !this.heatmapPath.equals("undefined");
    }

}
