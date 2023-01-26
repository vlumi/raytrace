package fi.misaki.raytrace;

import java.awt.*;

public record Sphere(
        Point3D center,
        double radius,
        Color color,
        int specular
) {
}
