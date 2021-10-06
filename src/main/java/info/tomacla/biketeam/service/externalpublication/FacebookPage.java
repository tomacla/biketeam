package info.tomacla.biketeam.service.externalpublication;

public class FacebookPage {

    private String id;
    private String name;

    public FacebookPage(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
