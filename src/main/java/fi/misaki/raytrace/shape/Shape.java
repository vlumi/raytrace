package fi.misaki.raytrace.shape;

import fi.misaki.raytrace.render.Point3D;

import java.awt.*;

public interface Shape {
    Color color();

    int specular();

    double reflective();

    Point3D normal(Point3D intersection);

    ShapeIntersection getClosestIntersection(Point3D origin, Point3D direction, double minDistance, double maxDistance);

    boolean isIntersecting(Point3D origin, Point3D direction, double minDistance, double maxDistance);
}
