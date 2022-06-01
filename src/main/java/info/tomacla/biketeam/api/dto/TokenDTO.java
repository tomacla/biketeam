package info.tomacla.biketeam.api.dto;

public class TokenDTO {

    public String accessToken;
    public String refreshToken;

    public static TokenDTO valueOf(String accessToken, String refreshToken) {
        TokenDTO dto = new TokenDTO();
        dto.accessToken = accessToken;
        dto.refreshToken = refreshToken;
        return dto;
    }

}
