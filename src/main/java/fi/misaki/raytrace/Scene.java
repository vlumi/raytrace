package fi.misaki.raytrace;

import fi.misaki.raytrace.light.AmbientLight;
import fi.misaki.raytrace.light.DirectionalLight;
import fi.misaki.raytrace.light.Light;
import fi.misaki.raytrace.light.PointLight;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Scene {

    // TODO: get from configuration file
    private static final Sphere[] SPHERES = {
            new Sphere(new Point3D(0, -1, 3), 1, new Color(255, 0, 0)),
            new Sphere(new Point3D(2, 0, 4), 1, new Color(0, 0, 255)),
            new Sphere(new Point3D(-2, 0, 4), 1, new Color(0, 255, 0)),
            new Sphere(new Point3D(0, -5001, 0), 5000, new Color(255, 255, 0))
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
        double closest = Double.MAX_VALUE;
        Sphere closestSphere = null;
        for (Sphere sphere : SPHERES) {
            double[] intersections = intersectRaySphere(camera, viewPort, sphere);
            for (double intersection : intersections) {
                if (intersection >= minDistance && intersection <= maxDistance && intersection < closest) {
                    closest = intersection;
                    closestSphere = sphere;
                }
            }
        }
        if (closestSphere == null) {
            return backgroundColor;
        }
        Point3D intersection = camera.plus(viewPort.multiply(closest));
        Point3D normal = intersection.minus(closestSphere.center());
        normal = normal.divide(normal.length());
        float intensity = (float) computeLighting(intersection, normal);
        Color color = closestSphere.color();
        float[] hsbValues = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsbValues[0], hsbValues[1], hsbValues[2] * intensity);
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

    private double computeLighting(Point3D target, Point3D normal) {
        return Arrays.stream(LIGHTS)
                .map(light ->
                        switch (light) {
                            case AmbientLight l -> l.getIntensity();
                            case DirectionalLight l -> l.getIntensity(normal);
                            case PointLight l -> l.getIntensity(target, normal);
                            default -> 0.0;
                        }
                )
                .reduce(0.0, (a, b) -> a + b);
    }
}
