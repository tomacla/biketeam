package info.tomacla.biketeam.service.mattermost;

import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.datatype.Dates;
import info.tomacla.biketeam.domain.message.Message;
import info.tomacla.biketeam.domain.message.MessageHolder;
import info.tomacla.biketeam.domain.message.MessageTargetType;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.service.*;
import info.tomacla.biketeam.service.amqp.dto.TeamEntityDTO;
import info.tomacla.biketeam.service.url.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MattermostService {

    private static final Logger log = LoggerFactory.getLogger(MattermostService.class);

    @Autowired
    private UrlService urlService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private RideService rideService;

    @Autowired
    private TripService tripService;

    @Autowired
    private MessageService messageService;

    public boolean isConfigured(Team team) {
        return team.getIntegration().isMattermostConfigured();
    }

    @RabbitListener(queues = Queues.RIDE_PUBLISHED_MATTERMOST)
    public void consumeRidePublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.RIDE_PUBLISHED_MATTERMOST);
            teamService.get(body.teamId).ifPresent(team -> {
                rideService.get(body.teamId, body.id).ifPresent(ride -> {

                    if (!team.getIntegration().isMattermostPublishRides()) {
                        return;
                    }

                    log.info("Publish ride {} to mattermost", ride.getId());

                    StringBuilder sb = new StringBuilder();
                    sb.append(ride.getTitle()).append("\n");
                    sb.append("RDV ").append(Dates.frenchDateFormat(ride.getDate())).append("\n");
                    sb.append(ride.getSortedGroups().stream().map(RideGroup::getName).collect(Collectors.joining(", "))).append("\n");
                    sb.append("Toutes les infos : ").append(urlService.getRideUrl(team, ride));

                    final String content = sb.toString();
                    this.send(team, team.getIntegration().getMattermostChannelID(), content);

                });

            });

        } catch (Exception e) {
            log.error("Error in event " + Queues.RIDE_PUBLISHED_MATTERMOST, e);
        }
    }

    @RabbitListener(queues = Queues.TRIP_PUBLISHED_MATTERMOST)
    public void consumeTripPublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.TRIP_PUBLISHED_MATTERMOST);
            teamService.get(body.teamId).ifPresent(team ->
                    tripService.get(body.teamId, body.id).ifPresent(trip -> {

                        if (!team.getIntegration().isMattermostPublishTrips()) {
                            return;
                        }

                        log.info("Publish trip {} to mattermost", trip.getId());

                        StringBuilder sb = new StringBuilder();
                        sb.append(trip.getTitle()).append("\n");
                        sb.append("Du ").append(Dates.frenchDateFormat(trip.getStartDate())).append(" au ").append(Dates.frenchDateFormat(trip.getEndDate())).append("\n");
                        sb.append("Toutes les infos : ").append(urlService.getTripUrl(team, trip));

                        final String content = sb.toString();
                        this.send(team, team.getIntegration().getMattermostChannelID(), content);

                    })
            );

        } catch (Exception e) {
            log.error("Error in event " + Queues.TRIP_PUBLISHED_MATTERMOST, e);
        }
    }

    @RabbitListener(queues = Queues.PUBLICATION_PUBLISHED_MATTERMOST)
    public void consumePublicationPublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.PUBLICATION_PUBLISHED_MATTERMOST);
            teamService.get(body.teamId).ifPresent(team ->
                    publicationService.get(body.teamId, body.id).ifPresent(publication -> {
                        if (!team.getIntegration().isMattermostPublishPublications()) {
                            return;
                        }

                        log.info("Publish publication {} to mattermost", publication.getId());

                        StringBuilder sb = new StringBuilder();
                        sb.append(publication.getTitle()).append("\n");
                        sb.append("Toutes les infos : ").append(urlService.getTeamUrl(team));

                        final String content = sb.toString();
                        this.send(team, team.getIntegration().getMattermostChannelID(), content);
                    })
            );

        } catch (Exception e) {
            log.error("Error in event " + Queues.PUBLICATION_PUBLISHED_MATTERMOST, e);
        }
    }


    public void notify(Team team, Message message, MessageHolder messageHolder) {

        if (team.getIntegration().getMattermostMessageChannelID() == null) {
            return;
        }

        log.info("Notify message {} to mattermost", message.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("Nouveau message").append("\n");
        sb.append(message.getContent()).append("\n");
        if (message.getType().equals(MessageTargetType.TRIP)) {
            sb.append("Pour répondre : ").append(urlService.getTripUrl(team, (Trip) messageHolder));
        } else if (message.getType().equals(MessageTargetType.RIDE)) {
            sb.append("Pour répondre : ").append(urlService.getRideUrl(team, (Ride) messageHolder));
        } else {
            sb.append("Pour répondre : ").append(urlService.getTeamUrl(team));
        }

        final String content = sb.toString();
        this.send(team, team.getIntegration().getMattermostMessageChannelID(), content);

    }

    private void send(Team team, String channelId, String content) {

        if (!isConfigured(team)) {
            return;
        }

        try {

            RestTemplate rest = new RestTemplate();

            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.set("Authorization", "Bearer " + team.getIntegration().getMattermostApiToken());
            Map<String, String> payload = new HashMap<>();
            payload.put("channel_id", channelId);
            payload.put("message", content);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, httpHeaders);

            rest.postForLocation(team.getIntegration().getMattermostApiEndpoint() + "/api/v4/posts", request);

        } catch (RestClientException e) {
            log.error("Error while publishing to mattermost", e);
        }

    }

    @RabbitListener(queues = Queues.RIDE_MESSAGE_PUBLISHED_MATTERMOST)
    public void consumeRideMessagePublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.RIDE_MESSAGE_PUBLISHED_MATTERMOST);
            teamService.get(body.teamId).ifPresent(team ->
                    messageService.getMessage(body.id).ifPresent(message -> {
                        rideService.get(body.teamId, message.getTargetId()).ifPresent(ride -> this.notify(team, message, ride));
                    })
            );

        } catch (Exception e) {
            log.error("Error in event " + Queues.RIDE_MESSAGE_PUBLISHED_MATTERMOST, e);
        }
    }

    @RabbitListener(queues = Queues.TRIP_MESSAGE_PUBLISHED_MATTERMOST)
    public void consumeTripMessagePublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.TRIP_MESSAGE_PUBLISHED_MATTERMOST);
            teamService.get(body.teamId).ifPresent(team ->
                    messageService.getMessage(body.id).ifPresent(message -> {

                        tripService.get(body.teamId, message.getTargetId()).ifPresent(trip -> this.notify(team, message, trip));

                    })
            );

        } catch (Exception e) {
            log.error("Error in event " + Queues.TRIP_MESSAGE_PUBLISHED_MATTERMOST, e);
        }
    }


}
