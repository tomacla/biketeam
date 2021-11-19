package info.tomacla.biketeam.startup;

import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.TeamService;
import info.tomacla.biketeam.service.file.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(2)
public class ChangeMapFilenameListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ChangeMapFilenameListener.class);

    @Autowired
    private MapService mapService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private FileService fileService;

    @Autowired
    private Environment env;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {


        final String execute = env.getProperty("listener.recalculate_map_listener");
        if (Boolean.parseBoolean(execute)) {
            log.info("Executing RecalculateMapListener");
            teamService.list().forEach(team -> mapService.listMaps(team.getId())
                    .stream()
                    .map(map -> mapService.get(map.getTeamId(), map.getId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(map -> {
                        fileService.moveFile(FileRepositories.GPX_FILES, team.getId(), map.getPermalink() + ".gpx", map.getId() + ".gpx");
                        fileService.moveFile(FileRepositories.FIT_FILES, team.getId(), map.getPermalink() + ".fit", map.getId() + ".fit");
                        fileService.moveFile(FileRepositories.MAP_IMAGES, team.getId(), map.getPermalink() + ".png", map.getId() + ".png");
                    }));
        }
    }

}
