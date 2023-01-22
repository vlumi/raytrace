package fi.misaki.raytrace;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Scene {

    private static final Sphere[] SPHERES = {
            new Sphere(new Dot(0, -1, 3), 1, new Color(255, 0, 0)),
            new Sphere(new Dot(2, 0, 4), 1, new Color(0, 0, 255)),
            new Sphere(new Dot(-2, 0, 4), 1, new Color(0, 255, 0))
    };



    private static final double PROJECTION_PLANE_DISTANCE = 1;


    private final Color backgroundColor;
    private final Dimension canvasDimension;
    private final DoubleDimension viewportDimension;
    private final Point canvasMin;
    private final Point canvasMax;
    private Dot camera;

    public Scene(Color backgroundColor, Dimension canvasDimension) {
        this.backgroundColor = backgroundColor;
        this.canvasDimension = canvasDimension;
        this.viewportDimension = new DoubleDimension(1, 1.0 * canvasDimension.height / canvasDimension.width);
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
        camera = new Dot(0, 0, 0);
        BufferedImage image = new BufferedImage(canvasDimension.width, canvasDimension.height, BufferedImage.TYPE_INT_RGB);
        for (int y = canvasMin.y; y < canvasMax.y; y++) {
            for (int x = canvasMin.x; x < canvasMax.x; x++) {
                Dot viewPort = canvasToViewPort(x, y);
                Color color = traceRay(camera, viewPort, 1, Double.MAX_VALUE);
                putPixel(image, x, y, color.getRGB());
            }
        }
        return image;
    }

    private Dot canvasToViewPort(double x, double y) {
        return new Dot(
                x * viewportDimension.width() / canvasDimension.width,
                y * viewportDimension.height() / canvasDimension.height,
                PROJECTION_PLANE_DISTANCE
        );
    }

    private Color traceRay(Dot camera, Dot viewPort, double minDistance, double maxDistance) {
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
        return closestSphere.color();
    }

    private double[] intersectRaySphere(Dot camera, Dot viewPort, Sphere sphere) {
        Dot co = camera.minus(sphere.center());

        double a = Dot.dot(viewPort, viewPort);
        double b = 2 * Dot.dot(co, viewPort);
        double c = Dot.dot(co, co) - sphere.radius() * sphere.radius();

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) {
            return new double[]{Double.MAX_VALUE, Double.MAX_VALUE};
        }
        return new double[]{
                (-b + Math.sqrt(discriminant)) / (2 * a),
                (-b - Math.sqrt(discriminant)) / (2 * a)
        };
    }

}
