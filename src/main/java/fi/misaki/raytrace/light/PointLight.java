package fi.misaki.raytrace.light;

import fi.misaki.raytrace.Point3D;

public class PointLight implements Light {
    private double intensity;
    private Point3D position;

    public PointLight(double intensity, Point3D position) {
        this.intensity = intensity;
        this.position = position;
    }

    public double getIntensity(Point3D target, Point3D normal) {
        Point3D direction = position.minus(target);
        double angle = normal.dot(direction);
        return angle > 0
                ? intensity * angle / (normal.length() * direction.length())
                : 0;
    }
}
