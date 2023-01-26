package fi.misaki.raytrace.light;

import fi.misaki.raytrace.Point3D;
import fi.misaki.raytrace.Scene;

public class PointLight implements Light {
    private double intensity;
    private Point3D position;

    public PointLight(double intensity, Point3D position) {
        this.intensity = intensity;
        this.position = position;
    }

    public double getIntensity(Scene scene, Point3D target, Point3D normal, Point3D toViewPort, int specular) {
        Point3D direction = position.minus(target);
        if (scene.getClosestShapeIntersection(target, direction, 0.001, 1).shape() != null) {
            return 0;
        }
        double angle = normal.dot(direction);
        return Light.getDiffuseIntensity(intensity, normal, angle, direction)
                + Light.getSpecularIntensity(intensity, normal, toViewPort, specular, direction);
    }

}
