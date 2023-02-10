package fi.misaki.raytrace.render;

public record Point3D(double x, double y, double z) {
    public Point3D plus(Vector3D vector) {
        return new Point3D(this.x + vector.x(), this.y + vector.y(), this.z + vector.z());
    }

    public Point3D minus(Vector3D vector) {
        return new Point3D(this.x - vector.x(), this.y - vector.y(), this.z - vector.z());
    }

}
