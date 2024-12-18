package info.tomacla.biketeam.service;

import info.tomacla.biketeam.common.amqp.Exchanges;
import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.amqp.RoutingKeys;
import info.tomacla.biketeam.common.file.FileRepositories;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.team.*;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.amqp.BrokerService;
import info.tomacla.biketeam.service.amqp.dto.TeamDTO;
import info.tomacla.biketeam.service.file.FileService;
import info.tomacla.biketeam.service.image.ImageService;
import info.tomacla.biketeam.service.permalink.AbstractPermalinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeamService extends AbstractPermalinkService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    private TeamRepository teamRepository;

    private BrokerService brokerService;

    private ImageService imageService;
    private FileService fileService;

    @Autowired
    public TeamService(TeamRepository teamRepository, FileService fileService, BrokerService brokerService, FileService fileService1) {
        this.teamRepository = teamRepository;
        this.brokerService = brokerService;
        this.imageService = new ImageService(FileRepositories.MISC_IMAGES, fileService);
        this.fileService = fileService1;
    }

    public long count() {
        return teamRepository.count(SearchTeamSpecification.publiclyVisible());
    }

    public Optional<Team> get(String teamId) {
        return teamRepository.findById(teamId.toLowerCase());
    }

    @Transactional
    public void save(Team team) {
        log.info("Team {} is updated", team.getTeamId());

        boolean isNew = team.isNew();
        team = teamRepository.save(team);
        if (isNew) {
            this.initTeamImage(team);
        }

        if (team.getIntegration().isHeatmapConfigured()) {
            brokerService.sendToBroker(Exchanges.TASK, RoutingKeys.TASK_GENERATE_HEATMAP, TeamDTO.valueOf(team.getId()));
        }

    }

    public List<Team> getLast4() {
        Sort sort = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(0, 4, sort);
        SearchTeamSpecification spec = SearchTeamSpecification.publiclyVisible();
        return teamRepository.findAll(spec, pageable).getContent();
    }

    public List<LastTeamData> getLastTeamData(Set<String> teamIds) {
        return teamRepository.findLastData(teamIds);
    }

    public List<Team> getUserTeams(User user) {
        Sort sort = Sort.by(Sort.Order.asc("name").ignoreCase());
        SearchTeamSpecification spec = SearchTeamSpecification.ofUser(user);
        return teamRepository.findAll(spec, sort);
    }

    public Page<Team> searchTeams(int page, int pageSize, String name, List<Visibility> visibilities) {
        return this.searchTeams(page, pageSize, name, visibilities, Sort.Order.asc("name").ignoreCase());
    }

    public Page<Team> searchTeams(int page, int pageSize, String name, List<Visibility> visibilities, Sort.Order sortOrder) {
        Sort sort = Sort.by(sortOrder);
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        SearchTeamSpecification spec = SearchTeamSpecification.search(name, visibilities);
        return teamRepository.findAll(spec, pageable);
    }

    public List<Team> list() {
        return teamRepository.findAll(SearchTeamSpecification.all(), Sort.by("name").ascending());
    }

    @Transactional
    public void delete(String teamId) {
        get(teamId).ifPresent(team -> {
            team.setDeletion(true);
            save(team);
        });
    }


    @Override
    public boolean permalinkExists(String permalink) {
        return get(permalink).isPresent();
    }

    @RabbitListener(queues = Queues.TASK_CLEAN_TEAM_FILES)
    public void cleanTeamFiles() {
        final Set<String> existingTeamIds = list().stream().map(Team::getId).collect(Collectors.toSet());
        fileService.cleanTeamFiles(existingTeamIds);
    }


    private void initTeamImage(Team newTeam) {
        imageService.save(newTeam.getId(), "logo", getClass().getResourceAsStream("/default-images/empty.png"),
                "empty.png");
    }

    public void saveImage(String teamId, InputStream is, String fileName) {
        imageService.save(teamId, "logo", is, fileName);
    }

    public void deleteImage(String teamId) {
        imageService.delete(teamId, "logo");
    }

    public Optional<ImageDescriptor> getImage(String teamId) {
        return imageService.get(teamId, "logo");
    }


}
