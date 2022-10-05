package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.domain.user.User;

public class UserDTO {

    public String id;
    public boolean admin;
    public Long stravaId;
    public String firstName;
    public String lastName;
    public String city;
    public String email;
    public UserEmailPreferencesDTO emailPreferences;

    public static UserDTO valueOf(User user) {

        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.id = user.getId();
        dto.admin = user.isAdmin();
        dto.stravaId = user.getStravaId();
        dto.firstName = user.getFirstName();
        dto.lastName = user.getLastName();
        dto.city = user.getCity();
        dto.email = user.getEmail();
        dto.emailPreferences = new UserEmailPreferencesDTO();
        dto.emailPreferences.trips = user.isEmailPublishTrips();
        dto.emailPreferences.rides = user.isEmailPublishRides();
        dto.emailPreferences.publications = user.isEmailPublishPublications();
        return dto;

    }

    public static class UserEmailPreferencesDTO {

        public boolean trips;
        public boolean rides;
        public boolean publications;

    }

}
