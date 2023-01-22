package fi.misaki.raytrace;

public record Dot(double x, double y, double z) {
    public static double dot(Dot a, Dot b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    public Dot minus(Dot other) {
        return new Dot(
                this.x - other.x,
                this.y - other.y,
                this.z - other.z
        );
    }
}
