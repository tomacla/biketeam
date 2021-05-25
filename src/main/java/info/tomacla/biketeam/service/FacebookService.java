package info.tomacla.biketeam.service;

import com.restfb.*;
import com.restfb.scope.FacebookPermissions;
import com.restfb.scope.ScopeBuilder;
import com.restfb.types.Account;
import com.restfb.types.GraphResponse;
import info.tomacla.biketeam.common.Dates;
import info.tomacla.biketeam.common.FileExtension;
import info.tomacla.biketeam.common.FileRepositories;
import info.tomacla.biketeam.domain.global.SiteIntegration;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class FacebookService {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private FileService fileService;

    @Value("${site.url}")
    private String siteUrl;

    public void publish(Ride ride) {

        StringBuilder sb = new StringBuilder();
        sb.append(ride.getTitle()).append(" - ").append(Dates.frenchDateFormat(ride.getDate())).append("\n");
        sb.append(ride.getDescription()).append("\n\n");
        ride.getGroups().forEach(group -> {
            sb.append(group.getName()).append(" - ");
            sb.append(group.getLowerSpeed()).append("/").append(group.getUpperSpeed()).append(" km/h").append("\n");
            sb.append("Départ ").append(Dates.formatTime(group.getMeetingTime())).append(" - ");
            sb.append(group.getMeetingLocation()).append("\n");
            if (group.getMapId() != null) {
                sb.append("Map : ").append(siteUrl).append("/maps/").append(group.getMapId()).append("\n");
            }
            sb.append("\n");
        });

        final String content = sb.toString();
        if (ride.isImaged()) {
            final Path image = fileService.get(FileRepositories.RIDE_IMAGES, ride.getId() + fileService.exists(FileRepositories.RIDE_IMAGES, ride.getId(), FileExtension.byPriority()).get().getExtension());
            this.publish(content, image);
        } else {
            this.publish(content, null);
        }

    }

    public void publish(Publication publication) {
        this.publish(publication.getContent(), null);
    }

    private void publish(String content, Path image) {

        try {
            final SiteIntegration siteIntegration = configurationService.getSiteIntegration();
            if (siteIntegration.getFacebookConfigurationStep() == 4) {

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

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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

        final SiteIntegration siteIntegration = configurationService.getSiteIntegration();

        try {
            DefaultFacebookClient facebookClient = new DefaultFacebookClient(Version.LATEST);

            ScopeBuilder scopeBuilder = new ScopeBuilder();
            scopeBuilder.addPermission(FacebookPermissions.PAGES_MANAGE_POSTS);

            return facebookClient.getLoginDialogUrl(
                    siteIntegration.getFacebookAppId(),
                    getRedirectUri(),
                    scopeBuilder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String getUserAccessToken(String code) {

        final SiteIntegration siteIntegration = configurationService.getSiteIntegration();

        try {
            DefaultFacebookClient facebookClient = new DefaultFacebookClient(Version.LATEST);

            final FacebookClient.AccessToken accessToken = facebookClient.obtainUserAccessToken(
                    siteIntegration.getFacebookAppId(),
                    siteIntegration.getFacebookAppSecret(),
                    getRedirectUri(),
                    code
            );

            return accessToken.getAccessToken();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getRedirectUri() {
        return siteUrl + "/admin/integration/facebook/login/";
    }

}
