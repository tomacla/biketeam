package info.tomacla.biketeam.common;

public class Rounder {

    public static long round(double value) {
        return Math.round(value);
    }

    public static double round1Decimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public static double round2Decimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

}
