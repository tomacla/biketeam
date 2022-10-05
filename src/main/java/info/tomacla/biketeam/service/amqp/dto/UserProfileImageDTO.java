package info.tomacla.biketeam.service.amqp.dto;

public class UserProfileImageDTO {

    public String id;
    public String profileImage;

    public static UserProfileImageDTO valueOf(String id, String profileImage) {
        UserProfileImageDTO dto = new UserProfileImageDTO();
        dto.id = id;
        dto.profileImage = profileImage;
        return dto;
    }
}
