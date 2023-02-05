package fi.misaki.raytrace.shape;

import fi.misaki.raytrace.light.Light;
import fi.misaki.raytrace.render.DistanceRange;
import fi.misaki.raytrace.render.Point3D;
import fi.misaki.raytrace.scene.Scene;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public interface Shape {
    static Color traceRay(
            Scene scene,
            Point3D origin,
            Point3D viewPort,
            DistanceRange range,
            int iteration
    ) {
        ShapeIntersection shapeIntersection = getClosestIntersection(
                scene.shapes(),
                origin,
                viewPort,
                range
        );
        Shape shape = shapeIntersection.shape();
        if (shape == null) {
            return scene.backgroundColor();
        }
        return computerColor(scene, shape, origin, viewPort, shapeIntersection.intersection(), iteration)
                .orElse(scene.backgroundColor());
    }

    private static ShapeIntersection getClosestIntersection(
            Shape[] shapes,
            Point3D origin,
            Point3D direction,
            DistanceRange range
    ) {
        return Arrays.stream(shapes)
                .map(shape -> shape.getClosestIntersection(origin, direction, range))
                .filter(Objects::nonNull)
                .reduce(
                        new ShapeIntersection(null, Double.MAX_VALUE),
                        (a, b) -> a.intersection() < b.intersection() ? a : b
                );
    }

    private static Optional<Color> computerColor(
            Scene scene,
            Shape shape,
            Point3D camera,
            Point3D viewPort,
            double shapeIntersection,
            int iteration
    ) {
        Point3D intersection = camera.plus(viewPort.multiply(shapeIntersection));
        Point3D normal = shape.normal(intersection);
        Color tint = Light.compute(
                scene,
                intersection,
                normal,
                viewPort.negate(),
                shape.specular()
        );
        Color localColor = Light.applyLight(shape.color(), tint);

        double reflective = shape.reflective();
        if (iteration <= 0 || reflective <= 0) {
            return Optional.of(localColor);
        }

        Point3D reflection = Light.getReflectedDirection(normal, viewPort.negate());
        Color reflectedColor = traceRay(
                scene,
                intersection,
                reflection,
                new DistanceRange(),
                iteration - 1
        );

        return Optional.of(
                Light.mix(
                        Light.applyLight(localColor, (1 - reflective)),
                        Light.applyLight(reflectedColor, reflective)
                )
        );
    }

    Color color();

    int specular();

    double reflective();

    Point3D normal(Point3D intersection);

    ShapeIntersection getClosestIntersection(Point3D origin, Point3D direction, DistanceRange range);

    boolean isIntersecting(Point3D origin, Point3D direction, DistanceRange range);
}
