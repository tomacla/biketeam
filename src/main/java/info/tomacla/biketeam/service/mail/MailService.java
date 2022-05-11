package info.tomacla.biketeam.service.mail;

import info.tomacla.biketeam.common.datatype.Dates;
import info.tomacla.biketeam.common.file.FileExtension;
import info.tomacla.biketeam.common.file.ImageDescriptor;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.trip.Trip;
import info.tomacla.biketeam.domain.user.User;
import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.TripService;
import info.tomacla.biketeam.service.UserService;
import info.tomacla.biketeam.service.broadcast.BroadcastService;
import info.tomacla.biketeam.service.file.ThumbnailService;
import info.tomacla.biketeam.service.url.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MailService implements BroadcastService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private RideService rideService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private TripService tripService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private UserService userService;

    @Autowired
    private MailSenderService mailSenderService;

    @Autowired
    private ThumbnailService thumbnailService;

    @Override
    public boolean isConfigured(Team team) {
        return mailSenderService.isSmtpConfigured();
    }

    public void broadcast(Team team, Ride ride) {

        final List<User> recipients = userService.listUsersWithMailActivated(team)
                .stream()
                .filter(User::isEmailPublishRides)
                .collect(Collectors.toList());

        if (recipients.size() > 0) {

            log.info("Publish ride {} to {} recipients", ride.getId(), recipients.size());

            StringBuilder sb = new StringBuilder();
            sb.append("<html>").append("<head></head>").append("<body>");
            sb.append("<p>").append(ride.getTitle()).append(" - ").append(Dates.frenchDateFormat(ride.getDate())).append("</p>");
            sb.append("<p>").append(getHtmlLink(urlService.getRideUrl(team, ride))).append("</p>");
            sb.append("<p>").append(ride.getDescription()).append("</p>");
            sb.append("<br/>");
            ride.getSortedGroups().forEach(group -> {
                sb.append("<p>").append(group.getName()).append(" - ");
                sb.append(Math.round(group.getLowerSpeed())).append("/").append(Math.round(group.getUpperSpeed())).append(" km/h").append("<br/>");
                sb.append("Départ ").append(Dates.formatTime(group.getMeetingTime())).append(" - ");
                sb.append(group.getMeetingLocation()).append("<br/>");
                if (group.getMap() != null) {
                    sb.append("Map : ").append(getHtmlLink(urlService.getMapUrl(team, group.getMap()))).append("<br/>");
                }
                sb.append("</p>");
            });
            if (ride.isImaged()) {
                sb.append("<p><img width=\"400\" src=\"cid:Image\" /></p>");
            }
            sb.append("<br/>").append("<p>").append(team.getName()).append("</p>");
            sb.append("</body>").append("</html>");

            final String content = sb.toString();
            if (ride.isImaged()) {
                rideService.getImage(team.getId(), ride.getId()).ifPresent(rideImage -> this.broadcast(team, recipients, ride.getTitle(), content, rideImage.getPath()));
            } else {
                this.broadcast(team, recipients, ride.getTitle(), content, null);
            }

        }

    }

    public void broadcast(Team team, Trip trip) {

        final List<User> recipients = userService.listUsersWithMailActivated(team)
                .stream()
                .filter(User::isEmailPublishTrips)
                .collect(Collectors.toList());

        if (recipients.size() > 0) {

            log.info("Publish trip {} to {} recipients", trip.getId(), recipients.size());

            StringBuilder sb = new StringBuilder();
            sb.append("<html>").append("<head></head>").append("<body>");
            sb.append("<p>").append(trip.getTitle()).append(" - Du ").append(Dates.frenchDateFormat(trip.getStartDate())).append(" au ").append(Dates.frenchDateFormat(trip.getEndDate())).append("</p>");
            sb.append("<p>").append(getHtmlLink(urlService.getTripUrl(team, trip))).append("</p>");
            sb.append("<p>").append(trip.getDescription()).append("</p>");
            sb.append("<p>").append(Math.round(trip.getLowerSpeed())).append("/").append(Math.round(trip.getUpperSpeed())).append(" km/h").append("<br/>");
            sb.append("Départ ").append(Dates.formatTime(trip.getMeetingTime())).append(" - ");
            sb.append(trip.getMeetingLocation()).append("<br/>");
            sb.append("</p>");
            sb.append("<br/>");
            trip.getSortedStages().forEach(stage -> {
                sb.append("<p>").append(stage.getName()).append(" - ").append(Dates.frenchDateFormat(stage.getDate())).append("<br/>");
                if (stage.getMap() != null) {
                    sb.append("Map : ").append(getHtmlLink(urlService.getMapUrl(team, stage.getMap()))).append("<br/>");
                }
                sb.append("</p>");
            });
            if (trip.isImaged()) {
                sb.append("<p><img width=\"400\" src=\"cid:Image\" /></p>");
            }
            sb.append("<br/>").append("<p>").append(team.getName()).append("</p>");
            sb.append("</body>").append("</html>");

            final String content = sb.toString();
            if (trip.isImaged()) {
                tripService.getImage(team.getId(), trip.getId()).ifPresent(tripImage -> this.broadcast(team, recipients, trip.getTitle(), content, tripImage.getPath()));
            } else {
                this.broadcast(team, recipients, trip.getTitle(), content, null);
            }

        }

    }

    public void broadcast(Team team, Publication publication) {

        final List<User> recipients = userService.listUsersWithMailActivated(team)
                .stream()
                .filter(User::isEmailPublishPublications)
                .collect(Collectors.toList());

        if (recipients.size() > 0) {

            log.info("Publish publication {} to {} recipients", publication.getId(), recipients.size());

            StringBuilder sb = new StringBuilder();
            sb.append("<html>").append("<head></head>").append("<body>");
            sb.append("<p>").append(publication.getTitle()).append("</p>");
            sb.append("<p>").append(getHtmlLink(urlService.getTeamUrl(team))).append("</p>");
            sb.append("<p>").append(publication.getContent()).append("</p>");
            if (publication.isImaged()) {
                sb.append("<p><img width=\"400\" src=\"cid:Image\" /></p>");
            }
            sb.append("<br/>").append("<p>").append(team.getName()).append("</p>");
            sb.append("</body>").append("</html>");

            final String content = sb.toString();

            if (publication.isImaged()) {
                publicationService.getImage(team.getId(), publication.getId()).ifPresent(pubImage -> this.broadcast(team, recipients, publication.getTitle(), content, pubImage.getPath()));
            } else {
                this.broadcast(team, recipients, publication.getTitle(), content, null);
            }

        }

    }

    private String getHtmlLink(String href) {
        return "<a href=\"" + href + "\">" + href + "</a>";
    }

    private void broadcast(Team team, List<User> recipients, String subject, String content, Path image) {

        if (!isConfigured(team)) {
            return;
        }

        try {

            final Set<String> tos = recipients.stream().map(User::getEmail).collect(Collectors.toSet());

            ImageDescriptor embedImage = null;
            if (image != null) {
                final FileExtension fileExtension = FileExtension.findByFileName(image.getFileName().toString()).get();
                final Path thumbnail = thumbnailService.resizeImage(image, 400, fileExtension);
                embedImage = ImageDescriptor.of(fileExtension, thumbnail);
            }
            mailSenderService.sendHiddenly(team, tos, subject, content, embedImage);

        } catch (Exception e) {
            log.error("Error while publishing by email : " + content, e);
        }

    }

}