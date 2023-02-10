package fi.misaki.raytrace.scene;

import fi.misaki.raytrace.render.Point3D;
import fi.misaki.raytrace.render.RotationMatrix;
import fi.misaki.raytrace.render.Vector3D;

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
    public RotationMatrix rotationMatrix() {
        double sinX = Math.sin(rotationRad.x());
        double cosX = Math.cos(rotationRad.x());
        double sinY = Math.sin(rotationRad.y());
        double cosY = Math.cos(rotationRad.y());
        double sinZ = Math.sin(rotationRad.z());
        double cosZ = Math.cos(rotationRad.z());

        return new RotationMatrix(
                new Vector3D(
                        cosY * cosZ,
                        sinX * sinY * cosZ - cosX * sinZ,
                        cosX * sinY * cosZ + sinX * sinZ
                ),
                new Vector3D(
                        cosY * sinZ,
                        sinX * sinY * sinZ + cosX * cosZ,
                        cosX * sinY * sinZ - sinX * cosZ
                ),
                new Vector3D(
                        -sinY,
                        sinX * cosY,
                        cosX * cosY
                )
        );
    }

}
