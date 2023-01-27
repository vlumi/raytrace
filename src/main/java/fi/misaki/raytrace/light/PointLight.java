package fi.misaki.raytrace.light;

import fi.misaki.raytrace.render.Point3D;

import java.util.function.BiFunction;

public class PointLight implements Light {
    private double intensity;
    private Point3D position;

    public PointLight(double intensity, Point3D position) {
        this.intensity = intensity;
        this.position = position;
    }

    public double getIntensity(BiFunction<Point3D, Point3D, Boolean> isInShadow,
                               Point3D target,
                               Point3D normal,
                               Point3D toViewPort,
                               int specular) {
        Point3D direction = position.minus(target);
        if (isInShadow.apply(target, direction)) {
            return 0;
        }
        double angle = normal.dot(direction);
        return Light.getDiffuseIntensity(intensity, normal, angle, direction)
                + Light.getSpecularIntensity(intensity, normal, toViewPort, specular, direction);
    }

}
