package info.tomacla.biketeam.service.gpx;

public class StandaloneGpx {

    private final double length;
    private final double positiveElevation;
    private final double negativeElevation;

    public StandaloneGpx(double length, double positiveElevation, double negativeElevation) {
        this.length = length;
        this.positiveElevation = positiveElevation;
        this.negativeElevation = negativeElevation;
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
