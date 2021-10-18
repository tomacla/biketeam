package info.tomacla.biketeam;

import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.service.TeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class RecalculateMapListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(RecalculateMapListener.class);

    @Autowired
    private MapService mapService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private Environment env;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        final String execute = env.getProperty("listener.recalculate_map_listener");
        if (execute != null && Boolean.parseBoolean(execute)) {
            log.info("Executing RecalculateMapListener");
            teamService.list().forEach(team -> mapService.listMaps(team.getId()).forEach(map -> mapService.refreshFiles(team.getId(), map.getId())));
        }
    }

}
