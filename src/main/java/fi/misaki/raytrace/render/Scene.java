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

    // TODO: get from configuration file
    private static final Shape[] SHAPES = {
            new Sphere(new Point3D(0, -1, 3), 1, new Color(255, 0, 0), 500),
            new Sphere(new Point3D(2, 0, 4), 1, new Color(0, 0, 255), 500),
            new Sphere(new Point3D(-2, 0, 4), 1, new Color(0, 255, 0), 10),
            new Sphere(new Point3D(0, -5001, 0), 5000, new Color(255, 255, 0), 1000)
    };
    // TODO: get from configuration file
    private static final Light[] LIGHTS = {
            new AmbientLight(0.2),
            new PointLight(0.6, new Point3D(2, 1, 0)),
            new DirectionalLight(0.2, new Point3D(1, 4, 4))
    };
    private static final double PROJECTION_PLANE_DISTANCE = 1;
    // TODO: get from configuration file
    private static double FOV_SCALE = 1;

    public Scene() {}


    public BufferedImage render(Color backgroundColor, Dimension canvasDimension) {
        Point3D camera = new Point3D(0, 0, 0);
        BufferedImage image = new BufferedImage(canvasDimension.width, canvasDimension.height, BufferedImage.TYPE_INT_RGB);
        Point canvasMin = new Point(-canvasDimension.width / 2, -canvasDimension.height / 2);
        Point canvasMax = new Point(canvasDimension.width / 2, canvasDimension.height / 2);

        for (int y = canvasMin.y; y < canvasMax.y; y++) {
            for (int x = canvasMin.x; x < canvasMax.x; x++) {
                Point3D viewPort = canvasToViewPort(canvasDimension, x, y);
                Color color = traceRay(camera, viewPort, 1, Double.MAX_VALUE, backgroundColor);

                int targetX = x - canvasMin.x;
                int targetY = canvasDimension.height - (y - canvasMin.y) - 1;
                putPixel(image, targetX, targetY, color.getRGB());
            }
        }
        return image;
    }

    private Point3D canvasToViewPort(Dimension canvasDimension, double x, double y) {
        DoubleDimension viewportDimension = new DoubleDimension(FOV_SCALE, FOV_SCALE * canvasDimension.height / canvasDimension.width);
        return new Point3D(
                x * viewportDimension.width() / canvasDimension.width,
                y * viewportDimension.height() / canvasDimension.height,
                PROJECTION_PLANE_DISTANCE
        );
    }

    private void putPixel(BufferedImage image, int x, int y, int color) {
        image.setRGB(x, y, color);
    }

    private Color traceRay(Point3D camera, Point3D viewPort, double minDistance, double maxDistance, Color backgroundColor) {
        ShapeIntersection shapeIntersection = getClosestShapeIntersection(camera, viewPort, minDistance, maxDistance);
        Shape shape = shapeIntersection.shape();
        if (shape == null) {
            return backgroundColor;
        }
        return switch (shape) {
            case Sphere sphere -> getColor(sphere, camera, viewPort, shapeIntersection.intersection());
            default -> backgroundColor;
        };
    }

    public ShapeIntersection getClosestShapeIntersection(Point3D origin, Point3D direction, double minDistance, double maxDistance) {
        return getClosestShapeIntersection(SHAPES, origin, direction, minDistance, maxDistance);
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

    public boolean isIntersectingShape(Point3D origin, Point3D direction, double minDistance, double maxDistance) {
        return isIntersectingShape(SHAPES, origin, direction, minDistance, maxDistance);
    }

    private static boolean isIntersectingShape(Shape[] shapes, Point3D origin, Point3D direction, double minDistance, double maxDistance) {
        return Arrays.stream(shapes)
                .anyMatch(shape -> shape.isIntersecting(origin, direction, minDistance, maxDistance));
    }

    private Color getColor(Sphere sphere, Point3D camera, Point3D viewPort, double shapeIntersection) {
        Point3D intersection = camera.plus(viewPort.multiply(shapeIntersection));
        Point3D normal = intersection.minus(sphere.center());
        normal = normal.divide(normal.length());
        double intensity = computeLighting(intersection, normal, viewPort.negate(), sphere.specular());
        return applyIntensity(sphere.color(), intensity);
    }

    private static Color applyIntensity(Color color, double intensity) {
        return new Color(
                applyIntensity(color.getRed(), intensity),
                applyIntensity(color.getGreen(), intensity),
                applyIntensity(color.getBlue(), intensity)
        );
    }

    private static int applyIntensity(int colorChannel, double intensity) {
        return Math.max(0, Math.min(255, (int) Math.round(intensity * colorChannel)));
    }

    private double computeLighting(Point3D target, Point3D normal, Point3D toViewPort, int specular) {
        return Arrays.stream(LIGHTS)
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
