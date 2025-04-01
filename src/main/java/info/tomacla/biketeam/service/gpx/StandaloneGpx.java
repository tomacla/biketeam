package info.tomacla.biketeam.service.gpx;

public class StandaloneGpx {

    private final String name;
    private final double length;
    private final double positiveElevation;
    private final double negativeElevation;

    public StandaloneGpx(String name, double length, double positiveElevation, double negativeElevation) {
        this.name = name;
        this.length = length;
        this.positiveElevation = positiveElevation;
        this.negativeElevation = negativeElevation;
    }

    public String getName() {
        return name;
    }

    public double getLength() {
        return length;
    }

    public double getPositiveElevation() {
        return positiveElevation;
    }

    public double getNegativeElevation() {
        return negativeElevation;
    }

}
