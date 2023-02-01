package fi.misaki.raytrace.render;

public record RotationMatrix(
        Point3D x,
        Point3D y,
        Point3D z
) {
    public static RotationMatrix from(Point3D from) {
        double sinX = Math.sin(from.x());
        double cosX = Math.cos(from.x());
        double sinY = Math.sin(from.y());
        double cosY = Math.cos(from.y());
        double sinZ = Math.sin(from.z());
        double cosZ = Math.cos(from.z());

        return new RotationMatrix(
                new Point3D(
                        cosY * cosZ,
                        sinX * sinY * cosZ - cosX * sinZ,
                        cosX * sinY * cosZ + sinX * sinZ
                ),
                new Point3D(
                        cosY * sinZ,
                        sinX * sinY * sinZ + cosX * cosZ,
                        cosX * sinY * sinZ - sinX * cosZ
                ),
                new Point3D(
                        -sinY,
                        sinX * cosY,
                        cosX * cosY
                )
        );
    }
}
