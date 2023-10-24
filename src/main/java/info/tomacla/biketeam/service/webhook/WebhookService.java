package info.tomacla.biketeam.service.webhook;

import info.tomacla.biketeam.common.amqp.Queues;
import info.tomacla.biketeam.common.datatype.Strings;
import info.tomacla.biketeam.domain.team.Team;
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

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

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
        return true;
    }

    @RabbitListener(queues = Queues.RIDE_PUBLISHED_WEBHOOK)
    public void consumeRidePublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.RIDE_PUBLISHED_WEBHOOK);
            teamService.get(body.teamId).ifPresent(team -> {
                rideService.get(body.teamId, body.id).ifPresent(ride -> {

                    if (Strings.isBlank(team.getIntegration().getWebhookRide())) {
                        return;
                    }

                    log.info("Publish ride {} to webhook", ride.getId());

                    RideWebhookDTO payload = new RideWebhookDTO();
                    payload.id = ride.getId();
                    payload.teamId = ride.getTeamId();

                    payload.teamUrl = urlService.getTeamUrl(team);
                    payload.rideUrl = urlService.getRideUrl(team, ride);
                    payload.imageUrl = ride.isImaged() ? urlService.getRideUrl(team, ride) + "/image" : null;

                    payload.startPlace = PlaceWebhookDTO.valueOf(ride.getStartPlace());
                    payload.endPlace = PlaceWebhookDTO.valueOf(ride.getEndPlace());

                    payload.type = ride.getType().name();
                    payload.date = ride.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    payload.title = ride.getTitle();
                    payload.description = ride.getDescription();

                    payload.groups = ride.getSortedGroups()
                            .stream().map(g -> {
                                RideWebhookDTO.RideGroupWebhookDTO dto = new RideWebhookDTO.RideGroupWebhookDTO();
                                dto.name = g.getName();
                                dto.averageSpeed = g.getAverageSpeed();
                                dto.meetingTime = g.getMeetingTime().format(DateTimeFormatter.ISO_LOCAL_TIME);
                                return dto;
                            }).collect(Collectors.toList());

                    this.send(team, team.getIntegration().getWebhookRide(), payload, RideWebhookDTO.class);

                });

            });

        } catch (Exception e) {
            log.error("Error in event " + Queues.RIDE_PUBLISHED_WEBHOOK, e);
        }
    }

    @RabbitListener(queues = Queues.TRIP_PUBLISHED_WEBHOOK)
    public void consumeTripPublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.TRIP_PUBLISHED_WEBHOOK);
            teamService.get(body.teamId).ifPresent(team ->
                    tripService.get(body.teamId, body.id).ifPresent(trip -> {

                        if (Strings.isBlank(team.getIntegration().getWebhookTrip())) {
                            return;
                        }

                        log.info("Publish trip {} to webhook", trip.getId());

                        TripWebhookDTO payload = new TripWebhookDTO();
                        payload.id = trip.getId();
                        payload.teamId = trip.getTeamId();

                        payload.teamUrl = urlService.getTeamUrl(team);
                        payload.tripUrl = urlService.getTripUrl(team, trip);
                        payload.imageUrl = urlService.getTripUrl(team, trip) + "/image";

                        payload.startDate = trip.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                        payload.endDate = trip.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                        payload.meetingTime = trip.getMeetingTime().format(DateTimeFormatter.ISO_LOCAL_TIME);
                        payload.type = trip.getType().name();

                        payload.title = trip.getTitle();
                        payload.description = trip.getDescription();

                        payload.startPlace = PlaceWebhookDTO.valueOf(trip.getStartPlace());
                        payload.endPlace = PlaceWebhookDTO.valueOf(trip.getEndPlace());

                        payload.stages = trip.getSortedStages().stream()
                                .map(s -> {
                                    TripWebhookDTO.TripStageWebhookDTO dto = new TripWebhookDTO.TripStageWebhookDTO();
                                    dto.id = s.getId();
                                    dto.name = s.getName();
                                    dto.date = s.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                                    return dto;
                                }).collect(Collectors.toList());


                        this.send(team, team.getIntegration().getWebhookTrip(), payload, TripWebhookDTO.class);

                    })
            );

        } catch (Exception e) {
            log.error("Error in event " + Queues.TRIP_PUBLISHED_WEBHOOK, e);
        }
    }

    @RabbitListener(queues = Queues.PUBLICATION_PUBLISHED_WEBHOOK)
    public void consumePublicationPublished(TeamEntityDTO body) {
        try {

            log.info("Received event on " + Queues.PUBLICATION_PUBLISHED_WEBHOOK);
            teamService.get(body.teamId).ifPresent(team ->
                    publicationService.get(body.teamId, body.id).ifPresent(publication -> {

                        if (Strings.isBlank(team.getIntegration().getWebhookPublication())) {
                            return;
                        }

                        log.info("Publish publication {} to webhook", publication.getId());

                        PublicationWebhookDTO payload = new PublicationWebhookDTO();
                        payload.id = publication.getId();
                        payload.teamId = publication.getTeamId();
                        payload.teamUrl = urlService.getTeamUrl(team);
                        payload.publicationUrl = urlService.getPublicationUrl(team, publication);
                        payload.imageUrl = urlService.getPublicationUrl(team, publication) + "/image";
                        payload.title = publication.getTitle();
                        payload.content = publication.getContent();


                        this.send(team, team.getIntegration().getWebhookPublication(), payload, PublicationWebhookDTO.class);

                    })
            );

        } catch (Exception e) {
            log.error("Error in event " + Queues.PUBLICATION_PUBLISHED_WEBHOOK, e);
        }
    }

    private <T> void send(Team team, String endpoint, T payload, Class<T> clazz) {

        if (!isConfigured(team)) {
            return;
        }

        try {

            RestTemplate rest = new RestTemplate();

            final HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<T> request = new HttpEntity<>(payload, httpHeaders);

            rest.postForLocation(endpoint, request);

        } catch (RestClientException e) {
            log.error("Error while publishing to webhook", e);
        }

    }

}
