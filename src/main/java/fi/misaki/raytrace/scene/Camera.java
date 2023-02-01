package fi.misaki.raytrace.scene;

import fi.misaki.raytrace.render.Point3D;

public record Camera(
        Point3D position,
        Point3D rotationRad
) {
}
