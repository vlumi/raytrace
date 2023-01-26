package fi.misaki.raytrace;

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

    private final Color backgroundColor;
    private final Dimension canvasDimension;
    private final DoubleDimension viewportDimension;
    private final Point canvasMin;
    private final Point canvasMax;
    private Point3D camera;

    public Scene(Color backgroundColor, Dimension canvasDimension) {
        this.backgroundColor = backgroundColor;
        this.canvasDimension = canvasDimension;
        this.viewportDimension = new DoubleDimension(FOV_SCALE, FOV_SCALE * canvasDimension.height / canvasDimension.width);
        canvasMin = new Point(-canvasDimension.width / 2, -canvasDimension.height / 2);
        canvasMax = new Point(canvasDimension.width / 2, canvasDimension.height / 2);
        render();
    }

    private void putPixel(BufferedImage image, int x, int y, int color) {
        int targetX = x - canvasMin.x;
        int targetY = canvasDimension.height - (y - canvasMin.y) - 1;
        image.setRGB(targetX, targetY, color);
    }

    public BufferedImage render() {
        camera = new Point3D(0, 0, 0);
        BufferedImage image = new BufferedImage(canvasDimension.width, canvasDimension.height, BufferedImage.TYPE_INT_RGB);
        for (int y = canvasMin.y; y < canvasMax.y; y++) {
            for (int x = canvasMin.x; x < canvasMax.x; x++) {
                Point3D viewPort = canvasToViewPort(x, y);
                Color color = traceRay(camera, viewPort, 1, Double.MAX_VALUE);
                putPixel(image, x, y, color.getRGB());
            }
        }
        return image;
    }

    private Point3D canvasToViewPort(double x, double y) {
        return new Point3D(
                x * viewportDimension.width() / canvasDimension.width,
                y * viewportDimension.height() / canvasDimension.height,
                PROJECTION_PLANE_DISTANCE
        );
    }

    private Color traceRay(Point3D camera, Point3D viewPort, double minDistance, double maxDistance) {
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

    private ShapeIntersection getClosestShapeIntersection(Shape[] shapes, Point3D origin, Point3D direction, double minDistance, double maxDistance) {
        return Arrays.stream(shapes)
                .map(shape ->
                        switch (shape) {
                            case Sphere sphere ->
                                    getClosestShapeIntersection(sphere, origin, direction, minDistance, maxDistance);
                            default -> null;
                        }
                )
                .filter(Objects::nonNull)
                .reduce(
                        new ShapeIntersection(null, Double.MAX_VALUE),
                        (a, b) -> a.intersection() < b.intersection() ? a : b
                );
    }

    private ShapeIntersection getClosestShapeIntersection(Sphere sphere, Point3D origin, Point3D direction, double minDistance, double maxDistance) {
        ShapeIntersection result = null;
        double minIntersection = Double.MAX_VALUE;
        double[] intersections = intersectRaySphere(origin, direction, sphere);
        for (double intersection : intersections) {
            if (intersection > minDistance && intersection <= maxDistance && intersection < minIntersection) {
                result = new ShapeIntersection(sphere, intersection);
            }
        }
        return result;
    }

    private Color getColor(Sphere sphere, Point3D camera, Point3D viewPort, double shapeIntersection) {
        Point3D intersection = camera.plus(viewPort.multiply(shapeIntersection));
        Point3D normal = intersection.minus(sphere.center());
        normal = normal.divide(normal.length());
        double intensity = computeLighting(intersection, normal, viewPort.negate(), sphere.specular());
        return applyIntensity(sphere.color(), intensity);
    }

    private Color applyIntensity(Color color, double intensity) {
        return new Color(
                applyIntensity(color.getRed(), intensity),
                applyIntensity(color.getGreen(), intensity),
                applyIntensity(color.getBlue(), intensity)
        );
    }

    private int applyIntensity(int colorChannel, double intensity) {
        return Math.max(0, Math.min(255, (int) Math.round(intensity * colorChannel)));
    }

    private double[] intersectRaySphere(Point3D camera, Point3D viewPort, Sphere sphere) {
        Point3D co = camera.minus(sphere.center());

        double a = viewPort.dot(viewPort);
        double b = 2 * co.dot(viewPort);
        double c = co.dot(co) - sphere.radius() * sphere.radius();

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return new double[]{Double.MAX_VALUE, Double.MAX_VALUE};
        }
        return new double[]{
                (-b + Math.sqrt(discriminant)) / (2 * a),
                (-b - Math.sqrt(discriminant)) / (2 * a)
        };
    }

    private double computeLighting(Point3D target, Point3D normal, Point3D toViewPort, int specular) {
        return Arrays.stream(LIGHTS)
                .map(light ->
                        switch (light) {
                            case AmbientLight l -> l.getIntensity();
                            case DirectionalLight l -> l.getIntensity(this, target, normal, toViewPort, specular);
                            case PointLight l -> l.getIntensity(this, target, normal, toViewPort, specular);
                            default -> 0.0;
                        }
                )
                .reduce(0.0, (a, b) -> a + b);
    }
}
