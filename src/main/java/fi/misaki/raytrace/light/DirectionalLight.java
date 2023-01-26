package fi.misaki.raytrace.light;

import fi.misaki.raytrace.Point3D;
import fi.misaki.raytrace.Scene;

public class DirectionalLight implements Light {
    private double intensity;
    private Point3D direction;

    public DirectionalLight(double intensity, Point3D direction) {
        this.intensity = intensity;
        this.direction = direction;
    }

    public double getIntensity(Scene scene, Point3D target, Point3D normal, Point3D toViewPort, int specular) {
        if (scene.getClosestShapeIntersection(target, direction, 0.001, 1).shape() != null) {
            return 0;
        }
        double angle = normal.dot(direction);
        return Light.getDiffuseIntensity(intensity, normal, angle, direction)
                + Light.getSpecularIntensity(intensity, normal, toViewPort, specular, direction);
    }
}
