package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.common.Point;
import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamConfiguration;
import info.tomacla.biketeam.domain.team.TeamDescription;
import info.tomacla.biketeam.domain.team.TeamIntegration;
import info.tomacla.biketeam.service.FileService;
import info.tomacla.biketeam.service.HeatmapService;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/{teamId}/admin")
public class AdminTeamConfigurationController extends AbstractController {

    @Autowired
    private FileService fileService;

    @Autowired
    private MapService mapService;

    @Value("${contact.email}")
    private String smtpFrom;

    @Autowired
    private HeatmapService heatmapService;

    @GetMapping
    public String getSiteDescription(@PathVariable("teamId") String teamId,
                                     Principal principal, Model model) {

        final Team team = checkTeam(teamId);


        final TeamDescription teamDescription = team.getDescription();

        EditTeamDescriptionForm form = EditTeamDescriptionForm.builder()
                .withDescription(teamDescription.getDescription())
                .withFacebook(teamDescription.getFacebook())
                .withTwitter(teamDescription.getTwitter())
                .withEmail(teamDescription.getEmail())
                .withPhoneNumber(teamDescription.getPhoneNumber())
                .withAddressStreetLine(teamDescription.getAddressStreetLine())
                .withAddressPostalCode(teamDescription.getAddressPostalCode())
                .withAddressCity(teamDescription.getAddressCity())
                .withOther(teamDescription.getOther())
                .get();

        addGlobalValues(principal, model, "Administration - Description", team);
        model.addAttribute("formdata", form);
        return "team_admin_description";
    }

    @PostMapping
    public String updateSiteDescription(@PathVariable("teamId") String teamId,
                                        Principal principal, Model model,
                                        EditTeamDescriptionForm form) {

        final Team team = checkTeam(teamId);


        final TeamDescription teamDescription = team.getDescription();

        try {

            teamDescription.setDescription(form.getDescription());
            teamDescription.setFacebook(form.getFacebook());
            teamDescription.setTwitter(form.getTwitter());
            teamDescription.setEmail(form.getEmail());
            teamDescription.setPhoneNumber(form.getPhoneNumber());
            teamDescription.setAddressStreetLine(form.getAddressStreetLine());
            teamDescription.setAddressPostalCode(form.getAddressPostalCode());
            teamDescription.setAddressCity(form.getAddressCity());
            teamDescription.setOther(form.getOther());
            teamService.save(team);

            addGlobalValues(principal, model, "Administration - Description", team);
            model.addAttribute("formdata", form);
            return "team_admin_description";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Description", team);
            model.addAttribute("errors", List.of(e.getMessage()));
            model.addAttribute("formdata", form);
            return "team_admin_description";
        }

    }

    @GetMapping(value = "/configuration")
    public String getSiteConfiguration(@PathVariable("teamId") String teamId,
                                       Principal principal, Model model) {

        final Team team = checkTeam(teamId);


        final TeamConfiguration teamConfiguration = team.getConfiguration();

        EditTeamConfigurationForm form = EditTeamConfigurationForm.builder()
                .withTimezone(teamConfiguration.getTimezone())
                .withDefaultSearchTags(teamConfiguration.getDefaultSearchTags())
                .withDefaultPage(teamConfiguration.getDefaultPage())
                .withFeedVisible(teamConfiguration.isFeedVisible())
                .withRidesVisible(teamConfiguration.isRidesVisible())
                .withTripsVisible(teamConfiguration.isTripsVisible())
                .get();

        addGlobalValues(principal, model, "Administration - Configuration", team);
        model.addAttribute("formdata", form);
        model.addAttribute("timezones", getAllAvailableTimeZones());
        model.addAttribute("tags", mapService.listTags(team.getId()));
        model.addAttribute("adminContact", smtpFrom);
        model.addAttribute("domain", teamConfiguration.getDomain());
        return "team_admin_configuration";
    }

    @PostMapping(value = "/configuration")
    public String updateSiteConfiguration(@PathVariable("teamId") String teamId,
                                          Principal principal,
                                          Model model,
                                          EditTeamConfigurationForm form) {

        final Team team = checkTeam(teamId);


        final TeamConfiguration teamConfiguration = team.getConfiguration();

        try {
            EditTeamConfigurationForm.EditTeamConfigurationFormParser parser = form.parser();

            teamConfiguration.setTimezone(parser.getTimezone());
            teamConfiguration.setDefaultSearchTags(parser.getDefaultSearchTags());
            teamConfiguration.setDefaultPage(parser.getDefaultPage());
            teamConfiguration.setFeedVisible(parser.isFeedVisible());
            teamConfiguration.setRidesVisible(parser.isRidesVisible());
            teamConfiguration.setTripsVisible(parser.isTripsVisible());
            teamService.save(team);

            addGlobalValues(principal, model, "Administration - Configuration", team);
            model.addAttribute("formdata", form);
            model.addAttribute("timezones", getAllAvailableTimeZones());
            model.addAttribute("tags", mapService.listTags(team.getId()));
            model.addAttribute("adminContact", smtpFrom);
            model.addAttribute("domain", teamConfiguration.getDomain());
            return "team_admin_configuration";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Configuration", team);
            model.addAttribute("formdata", form);
            model.addAttribute("timezones", getAllAvailableTimeZones());
            model.addAttribute("tags", mapService.listTags(team.getId()));
            model.addAttribute("adminContact", smtpFrom);
            model.addAttribute("domain", teamConfiguration.getDomain());
            return "team_admin_configuration";

        }

    }

    @GetMapping(value = "/page")
    public String getSitePage(@PathVariable("teamId") String teamId,
                              Principal principal, Model model) {

        final Team team = checkTeam(teamId);


        final TeamConfiguration teamConfiguration = team.getConfiguration();

        EditTeamPageForm form = EditTeamPageForm.builder()
                .withMarkdownPage(teamConfiguration.getMarkdownPage())
                .get();

        addGlobalValues(principal, model, "Administration - Configuration", team);
        model.addAttribute("formdata", form);
        return "team_admin_page_configuration";
    }

    @PostMapping(value = "/page")
    public String updateSitePage(@PathVariable("teamId") String teamId,
                                 Principal principal,
                                 Model model,
                                 EditTeamPageForm form) {

        final Team team = checkTeam(teamId);


        final TeamConfiguration teamConfiguration = team.getConfiguration();

        try {
            final EditTeamPageForm.EditTeamPageFormParser parser = form.parser();

            teamConfiguration.setMarkdownPage(parser.getMarkdownPage());
            teamService.save(team);

            addGlobalValues(principal, model, "Administration - Configuration", team);
            model.addAttribute("formdata", form);
            return "team_admin_page_configuration";

        } catch (Exception e) {
            addGlobalValues(principal, model, "Administration - Configuration", team);
            model.addAttribute("formdata", form);
            return "team_admin_page_configuration";

        }

    }

    @GetMapping(value = "/integration")
    public String getSiteIntegration(@PathVariable("teamId") String teamId,
                                     Principal principal, Model model) {

        final Team team = checkTeam(teamId);


        final TeamIntegration teamIntegration = team.getIntegration();

        EditTeamIntegrationForm form = EditTeamIntegrationForm.builder()
                .withFacebookGroupDetails(teamIntegration.isFacebookGroupDetails())
                .withFacebookPublishPublications(teamIntegration.isFacebookPublishPublications())
                .withFacebookPublishRides(teamIntegration.isFacebookPublishRides())
                .withFacebookPublishTrips(teamIntegration.isFacebookPublishTrips())
                .withMattermostApiEndpoint(teamIntegration.getMattermostApiEndpoint())
                .withMattermostApiToken(teamIntegration.getMattermostApiToken())
                .withMattermostChannelID(teamIntegration.getMattermostChannelID())
                .withMattermostPublishPublications(teamIntegration.isMattermostPublishPublications())
                .withMattermostPublishRides(teamIntegration.isMattermostPublishRides())
                .withMattermostPublishTrips(teamIntegration.isMattermostPublishTrips())
                .withHeatmapCenter(teamIntegration.getHeatmapCenter())
                .withHeatmapDisplay(teamIntegration.isHeatmapDisplay())
                .get();

        addGlobalValues(principal, model, "Administration - Intégrations", team);
        model.addAttribute("formdata", form);
        model.addAttribute("adminContact", smtpFrom);
        return "team_admin_integration";
    }

    @PostMapping(value = "/integration")
    public String updateSiteIntegration(@PathVariable("teamId") String teamId,
                                        Principal principal, Model model,
                                        EditTeamIntegrationForm form) {

        final Team team = checkTeam(teamId);


        final TeamIntegration teamIntegration = team.getIntegration();

        try {

            Point beforeEdit = team.getIntegration().getHeatmapCenter();

            final EditTeamIntegrationForm.EditTeamIntegrationFormParser parser = form.parser();

            teamIntegration.setFacebookGroupDetails(parser.isFacebookGroupDetails());
            teamIntegration.setFacebookPublishRides(parser.isFacebookPublishRides());
            teamIntegration.setFacebookPublishPublications(parser.isFacebookPublishPublications());
            teamIntegration.setFacebookPublishTrips(parser.isFacebookPublishTrips());
            teamIntegration.setMattermostApiToken(parser.getMattermostApiToken());
            teamIntegration.setMattermostChannelID(parser.getMattermostChannelID());
            teamIntegration.setMattermostApiEndpoint(parser.getMattermostApiEndpoint());
            teamIntegration.setMattermostPublishPublications(parser.isMattermostPublishPublications());
            teamIntegration.setMattermostPublishRides(parser.isMattermostPublishRides());
            teamIntegration.setMattermostPublishTrips(parser.isMattermostPublishTrips());
            teamIntegration.setHeatmapCenter(parser.getHeatmapCenter());
            teamIntegration.setHeatmapDisplay(parser.isHeatmapDisplay());
            teamService.save(team);

            if (team.getIntegration().isHeatmapConfigured() && !team.getIntegration().getHeatmapCenter().equals(beforeEdit)) {
                heatmapService.generateHeatmap(team);
            }

            addGlobalValues(principal, model, "Administration - Intégrations", team);
            model.addAttribute("formdata", form);
            model.addAttribute("adminContact", smtpFrom);
            return "team_admin_integration";

        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
            addGlobalValues(principal, model, "Administration - Intégrations", team);
            model.addAttribute("formdata", form);
            model.addAttribute("adminContact", smtpFrom);
            return "team_admin_integration";
        }

    }

    @GetMapping(value = "/logo")
    public String getLogo(@PathVariable("teamId") String teamId,
                          Principal principal,
                          Model model) {

        final Team team = checkTeam(teamId);


        addGlobalValues(principal, model, "Administration - Logo", team);
        return "team_admin_logo";
    }

    @PostMapping(value = "/logo")
    public String updateLogo(@PathVariable("teamId") String teamId,
                             Principal principal, Model model,
                             @RequestParam("file") MultipartFile file) {

        final Team team = checkTeam(teamId);


        try {
            teamService.saveImage(team.getId(), file.getInputStream(), file.getOriginalFilename());
        } catch (Exception e) {
            model.addAttribute("errors", List.of(e.getMessage()));
        } finally {
            addGlobalValues(principal, model, "Administration - Général", team);
        }

        return "team_admin_logo";

    }


}
