package info.tomacla.biketeam.service.webhook;

import java.util.List;

public class TripWebhookDTO {


    public String id;
    public String teamId;

    public String teamUrl;
    public String tripUrl;
    public String imageUrl;

    public String startDate;
    public String endDate;
    public String meetingTime;
    public String type;

    public String title;
    public String description;

    public PlaceWebhookDTO startPlace;
    public PlaceWebhookDTO endPlace;

    public List<TripStageWebhookDTO> stages;

    public static class TripStageWebhookDTO {

        public String id;
        public String date;
        public String name;

    }

}
