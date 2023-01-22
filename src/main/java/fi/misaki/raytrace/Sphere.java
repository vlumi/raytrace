package fi.misaki.raytrace;

import java.awt.*;

public record Sphere(
        Dot center,
        double radius,
        Color color
) {
}
