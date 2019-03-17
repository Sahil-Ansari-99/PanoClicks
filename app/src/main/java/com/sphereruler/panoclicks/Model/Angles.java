package com.sphereruler.panoclicks.Model;

public class Angles {
    public double polar;
    public double azimuth;

    public Angles(double polar, double azimuth) {
        this.polar = polar;
        this.azimuth = azimuth;
    }

    public double getPolar() {
        return polar;
    }

    public double getAzimuth() {
        return azimuth;
    }
}
