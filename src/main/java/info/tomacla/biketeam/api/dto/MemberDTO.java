package info.tomacla.biketeam.api.dto;

import info.tomacla.biketeam.domain.user.User;

public class MemberDTO {

    public String identity;
    public String profileImage;

    public static MemberDTO valueOf(User user) {

        if (user == null) {
            return null;
        }

        MemberDTO dto = new MemberDTO();
        dto.identity = user.getIdentity();
        dto.profileImage = user.getProfileImage();
        return dto;
    }

}
