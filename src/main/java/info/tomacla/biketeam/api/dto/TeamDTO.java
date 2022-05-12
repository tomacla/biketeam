package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.domain.team.Team;
import info.tomacla.biketeam.domain.team.Visibility;

import java.time.LocalDate;
import java.util.List;

public class TeamDTO {

    public String id;
    public String name;
    public String description;
    public String city;
    public String country;
    public LocalDate createdAt;
    public Visibility visibility;
    public String other;
    public TeamSocialDTO social;
    public TeamContactDTO contact;
    public TeamConfigurationDTO configuration;
    public boolean heatmap;

    public static class TeamSocialDTO {
        public String facebook;
        public String twitter;
        public String instagram;
    }

    public static class TeamContactDTO {
        public String email;
        public String phoneNumber;
        public String addressStreetLine;
        public String addressPostalCode;
        public String addressCity;
    }

    public static class TeamConfigurationDTO {

        public List<String> defaultSearchTags;
        public String defaultPage;
        public boolean feedVisible;
        public boolean ridesVisible;
        public boolean tripsVisible;
        public String timezone;

    }

    public static TeamDTO valueOf(Team team, boolean full) {

        if (team == null) {
            return null;
        }

        TeamDTO dto = new TeamDTO();
        dto.id = team.getId();
        dto.name = team.getName();
        dto.description = team.getDescription().getDescription();
        dto.city = team.getCity();
        dto.country = team.getCountry().name();
        dto.createdAt = team.getCreatedAt();
        dto.visibility = team.getVisibility();

        if (full) {
            dto.other = team.getDescription().getOther();
            dto.social = new TeamSocialDTO();
            dto.social.facebook = team.getDescription().getFacebook();
            dto.social.twitter = team.getDescription().getTwitter();
            dto.social.instagram = team.getDescription().getInstagram();
            dto.contact = new TeamContactDTO();
            dto.contact.email = team.getDescription().getEmail();
            dto.contact.phoneNumber = team.getDescription().getPhoneNumber();
            dto.contact.addressStreetLine = team.getDescription().getAddressStreetLine();
            dto.contact.addressPostalCode = team.getDescription().getAddressPostalCode();
            dto.contact.addressCity = team.getDescription().getAddressCity();
            dto.heatmap = team.getIntegration().isHeatmapDisplay() && team.getIntegration().isHeatmapConfigured();
            dto.configuration = new TeamConfigurationDTO();
            dto.configuration.defaultSearchTags = team.getConfiguration().getDefaultSearchTags();
            dto.configuration.defaultPage = team.getConfiguration().getDefaultPage().name();
            dto.configuration.feedVisible = team.getConfiguration().isFeedVisible();
            dto.configuration.ridesVisible = team.getConfiguration().isRidesVisible();
            dto.configuration.tripsVisible = team.getConfiguration().isTripsVisible();
            dto.configuration.timezone = team.getConfiguration().getTimezone();
        }

        return dto;

    }

}
