package fi.misaki.raytrace.scene;

import fi.misaki.raytrace.light.Light;
import fi.misaki.raytrace.shape.Shape;

import java.awt.*;

public record Scene(
        Camera camera,
        Color backgroundColor,
        Shape[] shapes,
        Light[] lights,
        double projectionPlaneDistance,
        double fovScale
) {
}
