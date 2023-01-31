package fi.misaki.raytrace.shape;

import fi.misaki.raytrace.render.Point3D;

import java.awt.*;

public record Sphere(
        Point3D center,
        double radius,
        Color color,
        int specular,
        double reflective
) implements Shape {

    @Override
    public Point3D normal(Point3D intersection) {
        Point3D normal = intersection.minus(center);
        return normal.divide(normal.length());
    }

    @Override
    public ShapeIntersection getClosestIntersection(Point3D origin, Point3D direction, double minDistance, double maxDistance) {
        ShapeIntersection result = null;
        double minIntersection = Double.MAX_VALUE;
        double[] intersections = intersectRaySphere(origin, direction);
        for (double intersection : intersections) {
            if (intersection > minDistance && intersection <= maxDistance && intersection < minIntersection) {
                result = new ShapeIntersection(this, intersection);
            }
        }
        return result;
    }

    @Override
    public boolean isIntersecting(Point3D origin, Point3D direction, double minDistance, double maxDistance) {
        double minIntersection = Double.MAX_VALUE;
        double[] intersections = intersectRaySphere(origin, direction);
        for (double intersection : intersections) {
            if (intersection > minDistance && intersection <= maxDistance && intersection < minIntersection) {
                return true;
            }
        }
        return false;
    }

    private double[] intersectRaySphere(Point3D camera, Point3D viewPort) {
        Point3D co = camera.minus(center);

        double a = viewPort.dot(viewPort);
        double b = 2 * co.dot(viewPort);
        double c = co.dot(co) - radius * radius;

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return new double[]{Double.MAX_VALUE, Double.MAX_VALUE};
        }
        return new double[]{
                (-b + Math.sqrt(discriminant)) / (2 * a),
                (-b - Math.sqrt(discriminant)) / (2 * a)
        };
    }
}
