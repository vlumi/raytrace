package fi.misaki.raytrace.light;

import fi.misaki.raytrace.Point3D;

public class DirectionalLight implements Light {
    private double intensity;
    private Point3D direction;

    public DirectionalLight(double intensity, Point3D direction) {
        this.intensity = intensity;
        this.direction = direction;
    }

    public double getIntensity(Point3D normal) {
        double angle = normal.dot(direction);
        return angle > 0
                ? intensity * angle / (normal.length() * direction.length())
                : 0;
    }
}
