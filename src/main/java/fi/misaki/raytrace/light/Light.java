package fi.misaki.raytrace.light;

import fi.misaki.raytrace.Point3D;
import fi.misaki.raytrace.Scene;

public interface Light {
    static boolean isInShadow(Scene scene, Point3D target, Point3D direction) {
        return scene.getClosestShapeIntersection(target, direction, 0.001, 1).shape() != null;
    }

    static double getDiffuseIntensity(double intensity, Point3D normal, double angle, Point3D direction) {
        return angle > 0 ? intensity * angle / (normal.length() * direction.length()) : 0;
    }

    static double getSpecularIntensity(double intensity, Point3D normal, Point3D toViewPort, int specular, Point3D direction) {
        if (specular <= 0) {
            return 0;
        }
        Point3D reflectedDirection = normal.multiply(2).multiply(normal.dot(direction)).minus(direction);
        double angle = reflectedDirection.dot(toViewPort);
        return angle > 0 ? intensity * Math.pow(angle / (reflectedDirection.length() * toViewPort.length()), specular) : 0;
    }
}
