package info.tomacla.biketeam.service.amqp.dto;

public class TeamDTO {

    public String teamId;

    public static TeamDTO valueOf(String teamId) {
        TeamDTO dto = new TeamDTO();
        dto.teamId = teamId;
        return dto;
    }

}
