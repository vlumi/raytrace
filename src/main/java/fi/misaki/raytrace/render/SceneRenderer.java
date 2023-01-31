package fi.misaki.raytrace.render;

import fi.misaki.raytrace.light.AmbientLight;
import fi.misaki.raytrace.light.DirectionalLight;
import fi.misaki.raytrace.light.Light;
import fi.misaki.raytrace.light.PointLight;
import fi.misaki.raytrace.scene.Scene;
import fi.misaki.raytrace.shape.Shape;
import fi.misaki.raytrace.shape.ShapeIntersection;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class SceneRenderer {

    private final Scene scene;
    private final int recursionDepth;

    public SceneRenderer(
            Scene scene,
            int recursionDepth
    ) {
        this.scene = scene;
        this.recursionDepth = recursionDepth;
    }

    public static ShapeIntersection getClosestShapeIntersection(Shape[] shapes, Point3D origin, Point3D direction, double minDistance, double maxDistance) {
        return Arrays.stream(shapes)
                .map(shape -> shape.getClosestIntersection(origin, direction, minDistance, maxDistance))
                .filter(Objects::nonNull)
                .reduce(
                        new ShapeIntersection(null, Double.MAX_VALUE),
                        (a, b) -> a.intersection() < b.intersection() ? a : b
                );
    }

    public static boolean isIntersectingShape(Shape[] shapes, Point3D origin, Point3D direction, double minDistance, double maxDistance) {
        return Arrays.stream(shapes)
                .anyMatch(shape -> shape.isIntersecting(origin, direction, minDistance, maxDistance));
    }

    private static Color computeLighting(Scene scene, Point3D target, Point3D normal, Point3D toViewPort, int specular) {
        return Arrays.stream(scene.lights())
                .map(light ->
                        switch (light) {
                            case AmbientLight l -> l.getTint();
                            case DirectionalLight l -> l.getTint(
                                    (direction) -> isInShadow(scene.shapes(), target, direction),
                                    target,
                                    normal,
                                    toViewPort,
                                    specular
                            );
                            case PointLight l -> l.getTint(
                                    (direction) -> isInShadow(scene.shapes(), target, direction),
                                    target,
                                    normal,
                                    toViewPort,
                                    specular
                            );
                            default -> Color.BLACK;
                        }
                )
                .reduce(Color.BLACK, Light::mix);
    }

    private static boolean isInShadow(Shape[] shapes, Point3D target, Point3D lightDirection) {
        return isIntersectingShape(shapes, target, lightDirection, 0.001, 1);
    }

    private static Color traceRay(
            Scene scene,
            Point3D camera,
            Point3D viewPort,
            double minDistance,
            double maxDistance,
            Color backgroundColor,
            int iteration
    ) {
        ShapeIntersection shapeIntersection = getClosestShapeIntersection(scene.shapes(), camera, viewPort, minDistance, maxDistance);
        Shape shape = shapeIntersection.shape();
        if (shape == null) {
            return backgroundColor;
        }
        return getColor(scene, shape, camera, viewPort, shapeIntersection.intersection(), iteration)
                .orElse(backgroundColor);
    }

    private static Optional<Color> getColor(Scene scene, Shape shape, Point3D camera, Point3D viewPort, double shapeIntersection, int iteration) {
        Point3D intersection = camera.plus(viewPort.multiply(shapeIntersection));
        Point3D normal = shape.normal(intersection);
        Color tint = computeLighting(scene, intersection, normal, viewPort.negate(), shape.specular());
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
                0.001,
                Double.MAX_VALUE,
                scene.backgroundColor(),
                iteration - 1
        );

        return Optional.of(
                Light.mix(
                        Light.applyLight(localColor, (1 - reflective)),
                        Light.applyLight(reflectedColor, reflective)
                )
        );
    }

    public BufferedImage render(Dimension canvasDimension) {
        Point3D camera = new Point3D(0, 0, 0);
        BufferedImage image = new BufferedImage(canvasDimension.width, canvasDimension.height, BufferedImage.TYPE_INT_RGB);
        Point canvasMin = new Point(-canvasDimension.width / 2, -canvasDimension.height / 2);
        Point canvasMax = new Point(canvasDimension.width / 2, canvasDimension.height / 2);

        for (int y = canvasMin.y; y < canvasMax.y; y++) {
            for (int x = canvasMin.x; x < canvasMax.x; x++) {
                Point3D viewPort = canvasToViewPort(canvasDimension, x, y);
                Color color = traceRay(scene, camera, viewPort, 1, Double.MAX_VALUE, scene.backgroundColor(), recursionDepth);

                int targetX = x - canvasMin.x;
                int targetY = canvasDimension.height - (y - canvasMin.y) - 1;
                putPixel(image, targetX, targetY, color.getRGB());
            }
        }
        return image;
    }

    private Point3D canvasToViewPort(Dimension canvasDimension, double x, double y) {
        DoubleDimension viewportDimension = new DoubleDimension(scene.fovScale(), scene.fovScale() * canvasDimension.height / canvasDimension.width);
        return new Point3D(
                x * viewportDimension.width() / canvasDimension.width,
                y * viewportDimension.height() / canvasDimension.height,
                scene.projectionPlaneDistance()
        );
    }

    private void putPixel(BufferedImage image, int x, int y, int color) {
        image.setRGB(x, y, color);
    }
}
