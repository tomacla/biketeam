package info.tomacla.biketeam.service.externalpublication;

import com.restfb.*;
import com.restfb.scope.FacebookPermissions;
import com.restfb.scope.ScopeBuilder;
import com.restfb.types.Account;
import com.restfb.types.GraphResponse;
import com.restfb.types.User;
import info.tomacla.biketeam.common.Dates;
import info.tomacla.biketeam.domain.parameter.Parameter;
import info.tomacla.biketeam.domain.parameter.ParameterRepository;
import info.tomacla.biketeam.domain.publication.Publication;
import info.tomacla.biketeam.domain.ride.Ride;
import info.tomacla.biketeam.domain.ride.RideGroup;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.service.PublicationService;
import info.tomacla.biketeam.service.RideService;
import info.tomacla.biketeam.service.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FacebookService implements ExternalPublicationService {

    private static final Logger log = LoggerFactory.getLogger(FacebookService.class);
    private static String FACEBOOK_TOKEN_PARAMETER_KEY = "FACEBOOK_TOKEN";
    @Autowired
    private RideService rideService;

    @Autowired
    private PublicationService publicationService;

    @Autowired
    private UrlService urlService;

    @Autowired
    private Environment env;

    @Autowired
    private ParameterRepository parameterRepository;

    @Override
    public boolean isApplicable(Team team) {
        return team.getIntegration().isFacebookConfigured();
    }

    public void publish(Team team, Ride ride) {

        log.info("Publish ride {} to facebook", ride.getId());

        StringBuilder sb = new StringBuilder();
        sb.append(ride.getTitle()).append("\n");
        sb.append("RDV ").append(Dates.frenchDateFormat(ride.getDate())).append("\n");
        if (!team.getIntegration().isFacebookGroupDetails()) {
            sb.append(ride.getSortedGroups().stream().map(RideGroup::getName).collect(Collectors.joining(", "))).append("\n");
        }
        sb.append("Toutes les infos : ").append(urlService.getRideUrl(team, ride.getId())).append("\n\n");
        sb.append(ride.getDescription()).append("\n\n");
        if (team.getIntegration().isFacebookGroupDetails()) {
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
        }

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

        if (!isApplicable(team)) {
            return;
        }

        final Optional<Parameter> facebookAccessToken = getFacebookAccessToken();
        if (facebookAccessToken.isEmpty()) {
            return;
        }

        try {

            final DefaultFacebookClient facebookClient = new DefaultFacebookClient(
                    facebookAccessToken.get().getValue(),
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
                            com.restfb.Parameter.with("message", content));
                } else {
                    publishClient.publish(team.getIntegration().getFacebookPageId() + "/feed",
                            GraphResponse.class,
                            com.restfb.Parameter.with("message", content));
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

    public Optional<String> getConnectedAccount() {

        final Optional<Parameter> facebookAccessToken = getFacebookAccessToken();
        if (facebookAccessToken.isEmpty()) {
            return Optional.empty();
        }

        try {

            DefaultFacebookClient facebookClient = new DefaultFacebookClient(
                    facebookAccessToken.get().getValue(),
                    Version.LATEST
            );

            User user = facebookClient.fetchObject("me", User.class);

            return Optional.of(user.getName());

        } catch (Exception e) {
            log.error("Error while fetching active profile", e);
            throw new RuntimeException(e);
        }

    }

    public List<FacebookPage> getAuthorizedPages() {

        final Optional<Parameter> facebookAccessToken = getFacebookAccessToken();
        if (facebookAccessToken.isEmpty()) {
            return new ArrayList<>();
        }

        try {

            DefaultFacebookClient facebookClient = new DefaultFacebookClient(
                    facebookAccessToken.get().getValue(),
                    Version.LATEST
            );

            List<FacebookPage> pages = new ArrayList<>();
            Connection<Account> connection = facebookClient.fetchConnection("/me/accounts", Account.class);

            for (List<Account> accounts : connection) {
                for (Account account : accounts) {
                    pages.add(new FacebookPage(account.getId(), account.getName()));
                }
            }

            return pages;

        } catch (Exception e) {
            log.error("Error while fetching pages", e);
            throw new RuntimeException(e);
        }
    }

    public String getLoginUrl() {

        try {

            DefaultFacebookClient facebookClient = new DefaultFacebookClient(Version.LATEST);

            ScopeBuilder scopeBuilder = new ScopeBuilder();
            scopeBuilder.addPermission(FacebookPermissions.PAGES_MANAGE_POSTS);
            scopeBuilder.addPermission(FacebookPermissions.PAGES_READ_ENGAGEMENT);
            scopeBuilder.addPermission(FacebookPermissions.PAGES_SHOW_LIST);

            return facebookClient.getLoginDialogUrl(
                    env.getProperty("facebook.app-id"),
                    getRedirectUri(),
                    scopeBuilder);
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

    public Optional<Parameter> getFacebookAccessToken() {
        return parameterRepository.findById(FACEBOOK_TOKEN_PARAMETER_KEY);
    }

    public void storeToken(String userAccessToken) {
        Parameter p = new Parameter();
        p.setName(FACEBOOK_TOKEN_PARAMETER_KEY);
        p.setValue(userAccessToken);
        parameterRepository.save(p);
    }

    public void deleteToken() {
        parameterRepository.findById(FACEBOOK_TOKEN_PARAMETER_KEY).ifPresent(p -> parameterRepository.delete(p));
    }

}
