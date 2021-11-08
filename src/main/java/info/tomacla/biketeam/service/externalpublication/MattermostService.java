package info.tomacla.biketeam.service.externalpublication;

import info.tomacla.biketeam.common.Dates;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.service.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MattermostService implements ExternalPublicationService {

    private static final Logger log = LoggerFactory.getLogger(MattermostService.class);

    @Autowired
    private UrlService urlService;

    @Override
    public boolean isConfigured(Team team) {
        return team.getIntegration().isMattermostConfigured();
    }

    @Override
    public void publish(Team team, Ride ride) {

        if (!team.getIntegration().isMattermostPublishRides()) {
            return;
        }

        log.info("Publish ride {} to mattermost", ride.getId());

        StringBuilder sb = new StringBuilder();
        sb.append(ride.getTitle()).append("\n");
        sb.append("RDV ").append(Dates.frenchDateFormat(ride.getDate())).append("\n");
        sb.append(ride.getSortedGroups().stream().map(RideGroup::getName).collect(Collectors.joining(", "))).append("\n");
        sb.append("Toutes les infos : ").append(urlService.getRideUrl(team, ride.getId()));

        final String content = sb.toString();
        this.publish(team, content);

    }

    @Override
    public void publish(Team team, Trip trip) {

        if (!team.getIntegration().isMattermostPublishTrips()) {
            return;
        }

        log.info("Publish trip {} to mattermost", trip.getId());

        StringBuilder sb = new StringBuilder();
        sb.append(trip.getTitle()).append("\n");
        sb.append("Du ").append(Dates.frenchDateFormat(trip.getStartDate())).append(" au ").append(Dates.frenchDateFormat(trip.getEndDate())).append("\n");
        sb.append("Toutes les infos : ").append(urlService.getTripUrl(team, trip.getId()));

        final String content = sb.toString();
        this.publish(team, content);

    }

    @Override
    public void publish(Team team, Publication publication) {

        if (!team.getIntegration().isMattermostPublishPublications()) {
            return;
        }

        log.info("Publish publication {} to mattermost", publication.getId());

        StringBuilder sb = new StringBuilder();
        sb.append(publication.getTitle()).append("\n");
        sb.append("Toutes les infos : ").append(urlService.getTeamUrl(team));

        final String content = sb.toString();
        this.publish(team, content);

    }

    private void publish(Team team, String content) {

        if (!isConfigured(team)) {
            return;
        }

        RestTemplate rest = new RestTemplate();

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Authorization", "Bearer " + team.getIntegration().getMattermostApiToken());
        Map<String, String> payload = new HashMap<>();
        payload.put("channel_id", team.getIntegration().getMattermostChannelID());
        payload.put("message", content);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, httpHeaders);

        rest.postForLocation(team.getIntegration().getMattermostApiEndpoint() + "/api/v4/posts", request);
    }

}
