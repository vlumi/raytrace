package fi.misaki.raytrace.light;

import fi.misaki.raytrace.render.Point3D;

public interface Light {
    static double getDiffuseIntensity(double intensity, Point3D normal, double angle, Point3D direction) {
        return angle > 0 ? intensity * angle / (normal.length() * direction.length()) : 0;
    }

    static double getSpecularIntensity(double intensity, Point3D normal, Point3D toViewPort, int specular, Point3D direction) {
        if (specular <= 0) {
            return 0;
        }
        Point3D reflectedDirection = getReflectedDirection(normal, direction);
        double angle = reflectedDirection.dot(toViewPort);
        return angle > 0 ? intensity * Math.pow(angle / (reflectedDirection.length() * toViewPort.length()), specular) : 0;
    }

    static Point3D getReflectedDirection(Point3D normal, Point3D direction) {
        return normal.multiply(2).multiply(normal.dot(direction)).minus(direction);
    }
}
