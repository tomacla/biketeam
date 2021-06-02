package info.tomacla.biketeam.domain.map;

public enum MapType {

    ROAD("Route"), GRAVEL("Gravel"), MTB("VTT");

    private String label;

    MapType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
