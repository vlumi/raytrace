package fi.misaki.raytrace.shape;

import fi.misaki.raytrace.render.Point3D;

public interface Shape {
    ShapeIntersection getClosestIntersection(Point3D origin, Point3D direction, double minDistance, double maxDistance);

    boolean isIntersecting(Point3D origin, Point3D direction, double minDistance, double maxDistance);
}
