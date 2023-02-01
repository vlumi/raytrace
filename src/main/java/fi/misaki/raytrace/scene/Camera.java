package fi.misaki.raytrace.scene;

import fi.misaki.raytrace.render.Point3D;

public record Camera(
        Point3D position,
        /**
         * Camera rotation in radian.
         *
         * x: forward pitch
         * y: right yaw
         * z: CCW roll
         */
        Point3D rotationRad
) {
}
