package fi.misaki.raytrace.render;

import java.util.Optional;

public record DistanceRange(
        double min,
        double max
) {
    private static final double EPSILON = 0.001;

    public DistanceRange(double max) {
        this(EPSILON, max);
    }

    public DistanceRange() {
        this(EPSILON, Double.MAX_VALUE);
    }

    public boolean includes(double value) {
        return value >= min && value <= max;
    }
}
