package fi.misaki.raytrace.shape;

import fi.misaki.raytrace.Point3D;

import java.awt.*;

public record Sphere(
        Point3D center,
        double radius,
        Color color,
        int specular
) implements Shape {
}
