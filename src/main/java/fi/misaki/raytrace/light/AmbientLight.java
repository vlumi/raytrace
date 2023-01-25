package fi.misaki.raytrace.light;

public class AmbientLight implements Light {
    private double intensity;

    public AmbientLight(double intensity) {
        this.intensity = intensity;
    }

    public double getIntensity() {
        return intensity;
    }
}
