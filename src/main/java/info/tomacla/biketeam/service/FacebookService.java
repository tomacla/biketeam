package info.tomacla.biketeam.service;

import com.restfb.*;
import com.restfb.scope.FacebookPermissions;
import com.restfb.scope.ScopeBuilder;
import com.restfb.types.Account;
import com.restfb.types.GraphResponse;
import info.tomacla.biketeam.common.Dates;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.team.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class FacebookService implements ExternalPublicationService {

    private static final Logger log = LoggerFactory.getLogger(FacebookService.class);

    @Autowired
    private TeamService teamService;

    @Autowired
    private RideService rideService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private Environment env;

    public void publish(Team team, Ride ride) {

        log.info("Publish ride {} to facebook", ride.getId());

        StringBuilder sb = new StringBuilder();
        sb.append(ride.getTitle()).append(" - ").append(Dates.frenchDateFormat(ride.getDate())).append("\n");
        sb.append(urlService.getRideUrl(team, ride.getId())).append("\n\n");
        sb.append(ride.getDescription()).append("\n\n");
        ride.getSortedGroups().forEach(group -> {
            sb.append(group.getName()).append(" - ");
            sb.append(Math.round(group.getLowerSpeed())).append("/").append(Math.round(group.getUpperSpeed())).append(" km/h").append("\n");
            sb.append("Départ ").append(Dates.formatTime(group.getMeetingTime())).append(" - ");
            sb.append(group.getMeetingLocation()).append("\n");
            if (group.getMapId() != null) {
                sb.append("Map : ").append(urlService.getMapUrl(team, group.getMapId())).append("\n");
            }
            sb.append("\n");
        });

        final String content = sb.toString();
        if (ride.isImaged()) {
            rideService.getImage(team.getId(), ride.getId()).ifPresent(rideImage -> this.publish(team, content, rideImage.getPath()));
        } else {
            this.publish(team, content, null);
        }

    }

    public void publish(Team team, Publication publication) {

        log.info("Publish publication {} to facebook", publication.getId());

        StringBuilder sb = new StringBuilder();
        sb.append(publication.getTitle()).append("\n");
        sb.append(urlService.getTeamUrl(team)).append("\n\n");
        sb.append(publication.getContent()).append("\n\n");

        final String content = sb.toString();
        if (publication.isImaged()) {
            publicationService.getImage(team.getId(), publication.getId()).ifPresent(pubImage -> this.publish(team, content, pubImage.getPath()));
        } else {
            this.publish(team, content, null);
        }

    }

    private void publish(Team team, String content, Path image) {

        if (!team.getIntegration().isFacebookConfigured()) {
            return;
        }

        try {

            final DefaultFacebookClient facebookClient = new DefaultFacebookClient(
                    team.getIntegration().getFacebookAccessToken(),
                    Version.LATEST
            );

            final Optional<String> optionalPageAccessToken = getPageAccessToken(team, facebookClient);
            if (optionalPageAccessToken.isPresent()) {

                final String token = optionalPageAccessToken.get();

                final FacebookClient publishClient = facebookClient.createClientWithAccessToken(token);

                if (image != null) {
                    final BinaryAttachment attachment = BinaryAttachment.with(image.getFileName().toString(), Files.readAllBytes(image));
                    publishClient.publish(team.getIntegration().getFacebookPageId() + "/photos",
                            GraphResponse.class,
                            attachment,
                            Parameter.with("message", content));
                } else {
                    publishClient.publish(team.getIntegration().getFacebookPageId() + "/feed",
                            GraphResponse.class,
                            Parameter.with("message", content));
                }

            }


        } catch (Exception e) {
            log.error("Error while publishing to facebook : " + content, e);
        }

    }

    private Optional<String> getPageAccessToken(Team team, DefaultFacebookClient facebookClient) {

        Connection<Account> connection = facebookClient.fetchConnection("/me/accounts", Account.class);

        for (List<Account> accounts : connection) {
            for (Account account : accounts) {
                if (account.getId().equals(team.getIntegration().getFacebookPageId())) {
                    return Optional.of(account.getAccessToken());
                }
            }
        }

        return Optional.empty();
    }

    public String getLoginUrl(String teamId) {

        try {

            DefaultFacebookClient facebookClient = new DefaultFacebookClient(Version.LATEST);

            ScopeBuilder scopeBuilder = new ScopeBuilder();
            scopeBuilder.addPermission(FacebookPermissions.PAGES_MANAGE_POSTS);

            return facebookClient.getLoginDialogUrl(
                    env.getProperty("facebook.app-id"),
                    getRedirectUri(),
                    scopeBuilder,
                    Parameter.with("state", teamId));
        } catch (Exception e) {
            log.error("Error while builing facebook login URL", e);
            throw new RuntimeException(e);
        }

    }

    public String getUserAccessToken(String code) {

        try {

            DefaultFacebookClient facebookClient = new DefaultFacebookClient(Version.LATEST);

            final FacebookClient.AccessToken accessToken = facebookClient.obtainUserAccessToken(
                    env.getProperty("facebook.app-id"),
                    env.getProperty("facebook.app-secret"),
                    getRedirectUri(),
                    code
            );

            return accessToken.getAccessToken();
        } catch (Exception e) {
            log.error("Error while getting user access token", e);
            throw new RuntimeException(e);
        }
    }

    private String getRedirectUri() {
        return urlService.getUrlWithSuffix("/integration/facebook/login/");
    }

}
