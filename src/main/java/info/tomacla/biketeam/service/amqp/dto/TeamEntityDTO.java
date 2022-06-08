package info.tomacla.biketeam.service.amqp.dto;

public class TeamEntityDTO {

    public String teamId;
    public String id;

    public static TeamEntityDTO valueOf(String teamId, String id) {
        TeamEntityDTO dto = new TeamEntityDTO();
        dto.teamId = teamId;
        dto.id = id;
        return dto;
    }

}
