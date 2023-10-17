package info.tomacla.biketeam.service.webhook;

import java.util.List;

public class RideWebhookDTO {

    public String id;
    public String teamId;

    public String teamUrl;
    public String rideUrl;
    public String imageUrl;

    public PlaceWebhookDTO startPlace;
    public PlaceWebhookDTO endPlace;

    public String type;
    public String date;
    public String title;
    public String description;

    public List<RideGroupWebhookDTO> groups;


    public static class RideGroupWebhookDTO {

        public String name;
        public double averageSpeed;
        public String meetingTime;


    }

}
