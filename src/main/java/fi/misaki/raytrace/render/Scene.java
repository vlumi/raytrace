package fi.misaki.raytrace.render;

import fi.misaki.raytrace.light.AmbientLight;
import fi.misaki.raytrace.light.DirectionalLight;
import fi.misaki.raytrace.light.Light;
import fi.misaki.raytrace.light.PointLight;
import fi.misaki.raytrace.shape.Shape;
import fi.misaki.raytrace.shape.ShapeIntersection;
import fi.misaki.raytrace.shape.Sphere;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;

public class Scene {

    private final Color backgroundColor;
    private final Shape[] shapes;
    private final Light[] lights;
    private final double projectionPlaneDistance;
    private final double fovScale;
    private final int recursionDepth;

    public Scene(
            Color backgroundColor,
            Shape[] shapes,
            Light[] lights,
            double projectionPlaneDistance,
            double fovScale,
            int recursionDepth
    ) {
        this.backgroundColor = backgroundColor;
        this.shapes = shapes;
        this.lights = lights;
        this.projectionPlaneDistance = projectionPlaneDistance;
        this.fovScale = fovScale;
        this.recursionDepth = recursionDepth;
    }

    private static ShapeIntersection getClosestShapeIntersection(Shape[] shapes, Point3D origin, Point3D direction, double minDistance, double maxDistance) {
        return Arrays.stream(shapes)
                .map(shape -> shape.getClosestIntersection(origin, direction, minDistance, maxDistance))
                .filter(Objects::nonNull)
                .reduce(
                        new ShapeIntersection(null, Double.MAX_VALUE),
                        (a, b) -> a.intersection() < b.intersection() ? a : b
                );
    }

    private static boolean isIntersectingShape(Shape[] shapes, Point3D origin, Point3D direction, double minDistance, double maxDistance) {
        return Arrays.stream(shapes)
                .anyMatch(shape -> shape.isIntersecting(origin, direction, minDistance, maxDistance));
    }

    private static Color applyIntensity(Color color, double intensity) {
        return new Color(
                applyIntensity(color.getRed(), intensity),
                applyIntensity(color.getGreen(), intensity),
                applyIntensity(color.getBlue(), intensity)
        );
    }

    private static int applyIntensity(int colorChannel, double intensity) {
        return clamp((int) Math.round(intensity * colorChannel), 0, 255);
    }

    private static Color sum(Color a, Color b) {
        return new Color(
                clamp(a.getRed() + b.getRed(), 0, 255),
                clamp(a.getGreen() + b.getGreen(), 0, 255),
                clamp(a.getBlue() + b.getBlue(), 0, 255)
        );
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public BufferedImage render(Dimension canvasDimension) {
        Point3D camera = new Point3D(0, 0, 0);
        BufferedImage image = new BufferedImage(canvasDimension.width, canvasDimension.height, BufferedImage.TYPE_INT_RGB);
        Point canvasMin = new Point(-canvasDimension.width / 2, -canvasDimension.height / 2);
        Point canvasMax = new Point(canvasDimension.width / 2, canvasDimension.height / 2);

        for (int y = canvasMin.y; y < canvasMax.y; y++) {
            for (int x = canvasMin.x; x < canvasMax.x; x++) {
                Point3D viewPort = canvasToViewPort(canvasDimension, x, y);
                Color color = traceRay(camera, viewPort, 1, Double.MAX_VALUE, backgroundColor, recursionDepth);

                int targetX = x - canvasMin.x;
                int targetY = canvasDimension.height - (y - canvasMin.y) - 1;
                putPixel(image, targetX, targetY, color.getRGB());
            }
        }
        return image;
    }

    private Point3D canvasToViewPort(Dimension canvasDimension, double x, double y) {
        DoubleDimension viewportDimension = new DoubleDimension(fovScale, fovScale * canvasDimension.height / canvasDimension.width);
        return new Point3D(
                x * viewportDimension.width() / canvasDimension.width,
                y * viewportDimension.height() / canvasDimension.height,
                projectionPlaneDistance
        );
    }

    private void putPixel(BufferedImage image, int x, int y, int color) {
        image.setRGB(x, y, color);
    }

    private Color traceRay(
            Point3D camera,
            Point3D viewPort,
            double minDistance,
            double maxDistance,
            Color backgroundColor,
            int iteration
    ) {
        ShapeIntersection shapeIntersection = getClosestShapeIntersection(camera, viewPort, minDistance, maxDistance);
        Shape shape = shapeIntersection.shape();
        if (shape == null) {
            return backgroundColor;
        }
        return switch (shape) {
            case Sphere sphere -> getColor(sphere, camera, viewPort, shapeIntersection.intersection(), iteration);
            default -> backgroundColor;
        };
    }

    public ShapeIntersection getClosestShapeIntersection(Point3D origin, Point3D direction, double minDistance, double maxDistance) {
        return getClosestShapeIntersection(shapes, origin, direction, minDistance, maxDistance);
    }

    public boolean isIntersectingShape(Point3D origin, Point3D direction, double minDistance, double maxDistance) {
        return isIntersectingShape(shapes, origin, direction, minDistance, maxDistance);
    }

    private Color getColor(Sphere sphere, Point3D camera, Point3D viewPort, double shapeIntersection, int iteration) {
        Point3D intersection = camera.plus(viewPort.multiply(shapeIntersection));
        Point3D normal = intersection.minus(sphere.center());
        normal = normal.divide(normal.length());
        double intensity = computeLighting(intersection, normal, viewPort.negate(), sphere.specular());
        Color localColor = applyIntensity(sphere.color(), intensity);

        double reflective = sphere.reflective();
        if (iteration <= 0 || reflective <= 0) {
            return localColor;
        }

        Point3D reflection = Light.getReflectedDirection(normal, viewPort.negate());
        Color reflectedColor = traceRay(intersection, reflection, 0.001, Double.MAX_VALUE, backgroundColor, iteration - 1);

        return sum(
                applyIntensity(localColor, (1 - reflective)),
                applyIntensity(reflectedColor, reflective)
        );
    }

    private double computeLighting(Point3D target, Point3D normal, Point3D toViewPort, int specular) {
        return Arrays.stream(lights)
                .map(light ->
                        switch (light) {
                            case AmbientLight l -> l.getIntensity();
                            case DirectionalLight l -> l.getIntensity(
                                    this::isInShadow,
                                    target,
                                    normal,
                                    toViewPort,
                                    specular
                            );
                            case PointLight l -> l.getIntensity(
                                    this::isInShadow,
                                    target,
                                    normal,
                                    toViewPort,
                                    specular
                            );
                            default -> 0.0;
                        }
                )
                .reduce(0.0, (a, b) -> a + b);
    }

    private boolean isInShadow(Point3D target, Point3D lightDirection) {
        return isIntersectingShape(target, lightDirection, 0.001, 1);
    }
}
