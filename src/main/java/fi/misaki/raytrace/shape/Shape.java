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
        ShapeIntersectionDistance shapeIntersection = getClosestIntersection(
                scene.shapes(),
                origin,
                viewPort,
                range
        );
        Shape shape = shapeIntersection.shape();
        if (shape == null) {
            return scene.backgroundColor();
        }
        return computerColor(scene, shape, origin, viewPort, shapeIntersection.distance(), iteration)
                .orElse(scene.backgroundColor());
    }

    private static ShapeIntersectionDistance getClosestIntersection(
            Shape[] shapes,
            Point3D origin,
            Point3D direction,
            DistanceRange range
    ) {
        return Arrays.stream(shapes)
                .map(shape -> shape.getClosestIntersection(origin, direction, range))
                .flatMap(Optional::stream)
                .reduce(
                        new ShapeIntersectionDistance(null, Double.MAX_VALUE),
                        (a, b) -> a.distance() < b.distance() ? a : b
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

        Color translucentColor = mixTranslucentColor(
                scene,
                shape,
                intersection,
                normal,
                viewPort,
                localColor,
                iteration
        );

        Color reflectedColor = mixReflectedColor(
                scene,
                intersection,
                normal,
                viewPort,
                translucentColor,
                shape.reflective(),
                iteration
        );

        return Optional.of(reflectedColor);
    }

    private static Color mixTranslucentColor(
            Scene scene,
            Shape shape,
            Point3D intersection,
            Point3D normal,
            Point3D viewPort,
            Color localColor,
            int iteration
    ) {
        if (shape.opacity() >= 1) {
            return localColor;
        }

        // TODO: refraction at entry
        Point3D entryDirection = viewPort;
        return shape.getClosestIntersection(intersection, entryDirection, new DistanceRange())
                .map(exitIntersectionDistance -> {
                    double distance = exitIntersectionDistance.distance();
                    Point3D exitIntersection = intersection.plus(entryDirection.multiply(distance));
                    double adjustedOpacity = shape.opacity() * distance / shape.nominalDiameter();
                    // TODO: refraction at exitIntersectionDistance
                    Point3D exitDirection = entryDirection;
                    Color translucentColor = traceRay(
                            scene,
                            exitIntersection,
                            exitDirection,
                            new DistanceRange(),
                            iteration
                    );
                    return Light.mix(
                            Light.applyLight(localColor, adjustedOpacity),
                            Light.applyLight(translucentColor, 1 - adjustedOpacity)
                    );
                })
                .orElse(localColor);
    }

    private static Color mixReflectedColor(
            Scene scene,
            Point3D intersection,
            Point3D normal,
            Point3D viewPort,
            Color localColor,
            double reflective,
            int iteration
    ) {
        if (iteration <= 0 || reflective <= 0) {
            return localColor;
        }

        Point3D reflection = Light.getReflectedDirection(normal, viewPort.negate());
        Color reflectedColor = traceRay(
                scene,
                intersection,
                reflection,
                new DistanceRange(),
                iteration - 1
        );

        return Light.mix(
                Light.applyLight(localColor, 1 - reflective),
                Light.applyLight(reflectedColor, reflective)
        );
    }

    Color color();

    int specular();

    double reflective();

    double opacity();

    double nominalDiameter();

    Point3D normal(Point3D intersection);

    Optional<ShapeIntersectionDistance> getClosestIntersection(Point3D origin, Point3D direction, DistanceRange range);

    boolean isIntersecting(Point3D origin, Point3D direction, DistanceRange range);
}
