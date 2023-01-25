package fi.misaki.raytrace;

public record Point3D(double x, double y, double z) {
    public double dot(Point3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Point3D minus(Point3D other) {
        return new Point3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }
}
