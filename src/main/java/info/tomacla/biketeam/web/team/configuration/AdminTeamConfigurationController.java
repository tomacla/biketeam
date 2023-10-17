package info.tomacla.biketeam.web.team.configuration;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.TeamConfiguration;
import info.tomacla.biketeam.domain.team.TeamDescription;
import info.tomacla.biketeam.domain.team.TeamIntegration;
import info.tomacla.biketeam.service.MapService;
import info.tomacla.biketeam.web.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(value = "/{teamId}/admin")
public class AdminTeamConfigurationController extends AbstractController {

    @Autowired
    private MapService mapService;

    @Value("${contact.email}")
    private String contactEmail;

    @GetMapping
    public RedirectView getAdmin(@PathVariable("teamId") String teamId,
                                 @ModelAttribute("error") String error,
                                 Principal principal, Model model) {

        final Team team = checkTeam(teamId);
        return viewHandler.redirectView(team, "/admin/rides");

    }

    @GetMapping(value = "/general")
    public String getSiteGeneral(@PathVariable("teamId") String teamId,
                                 @ModelAttribute("error") String error,
                                 Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        final TeamDescription teamDescription = team.getDescription();

        EditTeamGeneralForm form = EditTeamGeneralForm.builder()
                .withDescription(teamDescription.getDescription())
                .withName(team.getName())
                .withVisibility(team.getVisibility())
                .get();

        addGlobalValues(principal, model, "Administration - Général", team);
        model.addAttribute("formdata", form);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_general";

    }

    @PostMapping
    public RedirectView updateSiteGeneral(@PathVariable("teamId") String teamId,
                                          Principal principal, Model model,
                                          RedirectAttributes attributes,
                                          EditTeamGeneralForm form) {

        final Team team = checkTeam(teamId);
        final TeamDescription teamDescription = team.getDescription();
        final EditTeamGeneralForm.EditTeamGeneralFormParser parser = form.parser();

        try {

            teamDescription.setDescription(parser.getDescription());
            team.setName(parser.getName());
            team.setVisibility(parser.getVisibility());
            teamService.save(team);

            return viewHandler.redirectView(team, "/admin/general");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/general");
        }

    }

    @GetMapping(value = "/description")
    public String getSiteDescription(@PathVariable("teamId") String teamId,
                                     @ModelAttribute("error") String error,
                                     Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        final TeamDescription teamDescription = team.getDescription();

        EditTeamDescriptionForm form = EditTeamDescriptionForm.builder()
                .withFacebook(teamDescription.getFacebook())
                .withInstagram(teamDescription.getInstagram())
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
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_description";
    }

    @PostMapping(value = "/description")
    public RedirectView updateSiteDescription(@PathVariable("teamId") String teamId,
                                              Principal principal, Model model,
                                              RedirectAttributes attributes,
                                              EditTeamDescriptionForm form) {

        final Team team = checkTeam(teamId);
        final TeamDescription teamDescription = team.getDescription();
        final EditTeamDescriptionForm.EditTeamDescriptionFormParser parser = form.parser();

        try {

            teamDescription.setFacebook(parser.getFacebook());
            teamDescription.setInstagram(parser.getInstagram());
            teamDescription.setTwitter(parser.getTwitter());
            teamDescription.setEmail(parser.getEmail());
            teamDescription.setPhoneNumber(parser.getPhoneNumber());
            teamDescription.setAddressStreetLine(parser.getAddressStreetLine());
            teamDescription.setAddressPostalCode(parser.getAddressPostalCode());
            teamDescription.setAddressCity(parser.getAddressCity());
            teamDescription.setOther(parser.getOther());
            teamService.save(team);

            return viewHandler.redirectView(team, "/admin/description");


        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/description");
        }

    }

    @GetMapping(value = "/configuration")
    public String getSiteConfiguration(@PathVariable("teamId") String teamId,
                                       @ModelAttribute("error") String error,
                                       Principal principal, Model model) {

        final Team team = checkTeam(teamId);
        final TeamConfiguration teamConfiguration = team.getConfiguration();

        EditTeamConfigurationForm form = EditTeamConfigurationForm.builder()
                .withTimezone(teamConfiguration.getTimezone())
                .withDefaultSearchTags(teamConfiguration.getDefaultSearchTags())
                .withFeedVisible(teamConfiguration.isFeedVisible())
                .get();

        addGlobalValues(principal, model, "Administration - Configuration", team);
        model.addAttribute("formdata", form);
        model.addAttribute("timezones", getAllAvailableTimeZones());
        model.addAttribute("tags", mapService.listTags(team.getId()));
        model.addAttribute("adminContact", contactEmail);
        model.addAttribute("domain", teamConfiguration.getDomain());
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_configuration";
    }

    @PostMapping(value = "/configuration")
    public RedirectView updateSiteConfiguration(@PathVariable("teamId") String teamId,
                                                Principal principal, Model model,
                                                RedirectAttributes attributes,
                                                EditTeamConfigurationForm form) {

        final Team team = checkTeam(teamId);
        final TeamConfiguration teamConfiguration = team.getConfiguration();

        try {

            EditTeamConfigurationForm.EditTeamConfigurationFormParser parser = form.parser();

            teamConfiguration.setTimezone(parser.getTimezone());
            teamConfiguration.setDefaultSearchTags(parser.getDefaultSearchTags());
            teamConfiguration.setFeedVisible(parser.isFeedVisible());
            teamService.save(team);

            return viewHandler.redirectView(team, "/admin/configuration");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/configuration");

        }

    }

    @GetMapping(value = "/faq")
    public String getSitePage(@PathVariable("teamId") String teamId,
                              @ModelAttribute("error") String error,
                              Principal principal, Model model) {

        final Team team = checkTeam(teamId);
        final TeamConfiguration teamConfiguration = team.getConfiguration();

        EditTeamFAQForm form = EditTeamFAQForm.builder()
                .withMarkdownPage(teamConfiguration.getMarkdownPage())
                .get();

        addGlobalValues(principal, model, "Administration - Configuration", team);
        model.addAttribute("formdata", form);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_page_configuration";
    }

    @PostMapping(value = "/faq")
    public RedirectView updateSitePage(@PathVariable("teamId") String teamId,
                                       Principal principal, Model model,
                                       RedirectAttributes attributes,
                                       EditTeamFAQForm form) {

        final Team team = checkTeam(teamId);


        final TeamConfiguration teamConfiguration = team.getConfiguration();

        try {
            final EditTeamFAQForm.EditTeamPageFormParser parser = form.parser();

            teamConfiguration.setMarkdownPage(parser.getMarkdownPage());
            teamService.save(team);

            return viewHandler.redirectView(team, "/admin/faq");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/faq");
        }

    }

    @GetMapping(value = "/integration")
    public String getSiteIntegration(@PathVariable("teamId") String teamId,
                                     @ModelAttribute("error") String error,
                                     Principal principal, Model model) {

        final Team team = checkTeam(teamId);
        final TeamIntegration teamIntegration = team.getIntegration();

        EditTeamIntegrationForm form = EditTeamIntegrationForm.builder()
                .withMattermostApiEndpoint(teamIntegration.getMattermostApiEndpoint())
                .withMattermostApiToken(teamIntegration.getMattermostApiToken())
                .withMattermostChannelID(teamIntegration.getMattermostChannelID())
                .withMattermostMessageChannelID(teamIntegration.getMattermostMessageChannelID())
                .withMattermostPublishPublications(teamIntegration.isMattermostPublishPublications())
                .withMattermostPublishRides(teamIntegration.isMattermostPublishRides())
                .withMattermostPublishTrips(teamIntegration.isMattermostPublishTrips())
                .withHeatmapCenter(teamIntegration.getHeatmapCenter())
                .withHeatmapDisplay(teamIntegration.isHeatmapDisplay())
                .withWebhookRide(teamIntegration.getWebhookRide())
                .withWebhookTrip(teamIntegration.getWebhookTrip())
                .withWebhookPublication(teamIntegration.getWebhookPublication())
                .get();

        addGlobalValues(principal, model, "Administration - Intégrations", team);
        model.addAttribute("formdata", form);
        model.addAttribute("adminContact", contactEmail);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_integration";
    }

    @PostMapping(value = "/integration")
    public RedirectView updateSiteIntegration(@PathVariable("teamId") String teamId,
                                              Principal principal, Model model,
                                              RedirectAttributes attributes,
                                              EditTeamIntegrationForm form) {

        final Team team = checkTeam(teamId);
        final TeamIntegration teamIntegration = team.getIntegration();

        try {

            final EditTeamIntegrationForm.EditTeamIntegrationFormParser parser = form.parser();
            teamIntegration.setMattermostApiToken(parser.getMattermostApiToken());
            teamIntegration.setMattermostChannelID(parser.getMattermostChannelID());
            teamIntegration.setMattermostMessageChannelID(parser.getMattermostMessageChannelID());
            teamIntegration.setMattermostApiEndpoint(parser.getMattermostApiEndpoint());
            teamIntegration.setMattermostPublishPublications(parser.isMattermostPublishPublications());
            teamIntegration.setMattermostPublishRides(parser.isMattermostPublishRides());
            teamIntegration.setMattermostPublishTrips(parser.isMattermostPublishTrips());
            teamIntegration.setHeatmapCenter(parser.getHeatmapCenter());
            teamIntegration.setHeatmapDisplay(parser.isHeatmapDisplay());
            teamIntegration.setWebhookRide(parser.getWebhookRide());
            teamIntegration.setWebhookTrip(parser.getWebhookTrip());
            teamIntegration.setWebhookPublication(parser.getWebhookPublication());
            teamService.save(team);

            return viewHandler.redirectView(team, "/admin/integration");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/integration");
        }

    }

    @GetMapping(value = "/logo")
    public String getLogo(@PathVariable("teamId") String teamId,
                          @ModelAttribute("error") String error,
                          Principal principal, Model model) {

        final Team team = checkTeam(teamId);

        addGlobalValues(principal, model, "Administration - Logo", team);
        if (!ObjectUtils.isEmpty(error)) {
            model.addAttribute("errors", List.of(error));
        }
        return "team_admin_logo";
    }

    @PostMapping(value = "/logo")
    public RedirectView updateLogo(@PathVariable("teamId") String teamId,
                                   Principal principal, Model model,
                                   RedirectAttributes attributes,
                                   @RequestParam("file") MultipartFile file) {

        final Team team = checkTeam(teamId);

        try {

            teamService.saveImage(team.getId(), file.getInputStream(), file.getOriginalFilename());
            return viewHandler.redirectView(team, "/admin/logo");

        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return viewHandler.redirectView(team, "/admin/logo");
        }

    }


}