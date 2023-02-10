package fi.misaki.raytrace.render;

public record Vector3D(double x, double y, double z) {
    public Vector3D(Point3D from, Point3D to) {
        this(
                to.x() - from.x(),
                to.y() - from.y(),
                to.z() - from.z()
        );
    }

    public double dot(Vector3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3D negate() {
        return new Vector3D(-x, -y, -z);
    }

    public Vector3D plus(Vector3D other) {
        return new Vector3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3D minus(Vector3D other) {
        return new Vector3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3D multiply(double by) {
        return new Vector3D(x * by, y * by, z * by);
    }

    public Vector3D multiply(RotationMatrix by) {
        return new Vector3D(
                x * by.x().x + y * by.x().y + z * by.x().z,
                x * by.y().x + y * by.y().y + z * by.y().z,
                x * by.z().x + y * by.z().y + z * by.z().z
        );
    }

    public Vector3D divide(double by) {
        return new Vector3D(x / by, y / by, z / by);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

}
