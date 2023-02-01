package fi.misaki.raytrace.render;

public record Point3D(double x, double y, double z) {
    public double dot(Point3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Point3D negate() {
        return new Point3D(-x, -y, -z);
    }

    public Point3D plus(Point3D other) {
        return new Point3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Point3D minus(Point3D other) {
        return new Point3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Point3D multiply(double by) {
        return new Point3D(x * by, y * by, z * by);
    }

    public Point3D multiply(RotationMatrix by) {
        return new Point3D(
                x * by.x().x + y * by.x().y + z * by.x().z,
                x * by.y().x + y * by.y().y + z * by.y().z,
                x * by.z().x + y * by.z().y + z * by.z().z
        );
    }

    public Point3D divide(double by) {
        return new Point3D(x / by, y / by, z / by);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }
}
