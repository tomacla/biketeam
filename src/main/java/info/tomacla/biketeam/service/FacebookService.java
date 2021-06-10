package info.tomacla.biketeam.service;

import com.restfb.*;
import com.restfb.scope.FacebookPermissions;
import com.restfb.scope.ScopeBuilder;
import com.restfb.types.Account;
import com.restfb.types.GraphResponse;
import info.tomacla.biketeam.common.Dates;
import info.tomacla.biketeam.domain.global.SiteIntegration;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class FacebookService implements ExternalPublicationService {

    private static final Logger log = LoggerFactory.getLogger(FacebookService.class);

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private RideService rideService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private UrlService urlService;

    public void publish(Ride ride) {

        log.info("Publish ride {} to facebook", ride.getId());

        StringBuilder sb = new StringBuilder();
        sb.append(ride.getTitle()).append(" - ").append(Dates.frenchDateFormat(ride.getDate())).append("\n");
        sb.append(urlService.getRideUrl(ride.getId())).append("\n\n");
        sb.append(ride.getDescription()).append("\n\n");
        ride.getGroups().forEach(group -> {
            sb.append(group.getName()).append(" - ");
            sb.append(group.getLowerSpeed()).append("/").append(group.getUpperSpeed()).append(" km/h").append("\n");
            sb.append("Départ ").append(Dates.formatTime(group.getMeetingTime())).append(" - ");
            sb.append(group.getMeetingLocation()).append("\n");
            if (group.getMapId() != null) {
                sb.append("Map : ").append(urlService.getMapUrl(group.getMapId())).append("\n");
            }
            sb.append("\n");
        });

        final String content = sb.toString();
        if (ride.isImaged()) {
            rideService.getImage(ride.getId()).ifPresent(rideImage -> this.publish(content, rideImage.getPath()));
        } else {
            this.publish(content, null);
        }

    }

    public void publish(Publication publication) {

        log.info("Publish publication {} to facebook", publication.getId());

        StringBuilder sb = new StringBuilder();
        sb.append(publication.getTitle()).append("\n");
        sb.append(urlService.getUrl()).append("\n\n");
        sb.append(publication.getContent()).append("\n\n");

        final String content = sb.toString();
        if (publication.isImaged()) {
            publicationService.getImage(publication.getId()).ifPresent(pubImage -> this.publish(content, pubImage.getPath()));
        } else {
            this.publish(content, null);
        }

    }

    private void publish(String content, Path image) {

        final SiteIntegration siteIntegration = configurationService.getSiteIntegration();
        if (!siteIntegration.isFacebookConfigured()) {
            return;
        }

        try {


            final DefaultFacebookClient facebookClient = new DefaultFacebookClient(
                    siteIntegration.getFacebookAccessToken(),
                    Version.LATEST
            );

            final Optional<String> optionalPageAccessToken = getPageAccessToken(facebookClient);
            if (optionalPageAccessToken.isPresent()) {

                final String token = optionalPageAccessToken.get();

                final FacebookClient publishClient = facebookClient.createClientWithAccessToken(token);

                if (image != null) {
                    final BinaryAttachment attachment = BinaryAttachment.with(image.getFileName().toString(), Files.readAllBytes(image));
                    publishClient.publish(siteIntegration.getFacebookPageId() + "/photos",
                            GraphResponse.class,
                            attachment,
                            Parameter.with("message", content));
                } else {
                    publishClient.publish(siteIntegration.getFacebookPageId() + "/feed",
                            GraphResponse.class,
                            Parameter.with("message", content));
                }

            }


        } catch (Exception e) {
            log.error("Error while publishing to facebook : " + content, e);
        }

    }

    private Optional<String> getPageAccessToken(DefaultFacebookClient facebookClient) {

        final SiteIntegration siteIntegration = configurationService.getSiteIntegration();

        Connection<Account> connection = facebookClient.fetchConnection("/me/accounts", Account.class);

        for (List<Account> accounts : connection) {
            for (Account account : accounts) {
                if (account.getId().equals(siteIntegration.getFacebookPageId())) {
                    return Optional.of(account.getAccessToken());
                }
            }
        }

        return Optional.empty();
    }

    public String getLoginUrl() {

        try {
            final SiteIntegration siteIntegration = configurationService.getSiteIntegration();
            DefaultFacebookClient facebookClient = new DefaultFacebookClient(Version.LATEST);

            ScopeBuilder scopeBuilder = new ScopeBuilder();
            scopeBuilder.addPermission(FacebookPermissions.PAGES_MANAGE_POSTS);

            return facebookClient.getLoginDialogUrl(
                    siteIntegration.getFacebookAppId(),
                    getRedirectUri(),
                    scopeBuilder);
        } catch (Exception e) {
            log.error("Error while builing facebook login URL", e);
            throw new RuntimeException(e);
        }

    }

    public String getUserAccessToken(String code) {

        try {
            final SiteIntegration siteIntegration = configurationService.getSiteIntegration();
            DefaultFacebookClient facebookClient = new DefaultFacebookClient(Version.LATEST);

            final FacebookClient.AccessToken accessToken = facebookClient.obtainUserAccessToken(
                    siteIntegration.getFacebookAppId(),
                    siteIntegration.getFacebookAppSecret(),
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
        return urlService.getUrlWithSuffix("/admin/integration/facebook/login/");
    }

}
