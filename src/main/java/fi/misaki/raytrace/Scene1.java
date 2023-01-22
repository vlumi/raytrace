package fi.misaki.raytrace;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class Scene1 extends JPanel {

    private static final Sphere[] SPHERES = {
            new Sphere(new Dot(0, -1, 3), 1, new Color(255, 0, 0)),
            new Sphere(new Dot(2, 0, 4), 1, new Color(0, 0, 255)),
            new Sphere(new Dot(-2, 0, 4), 1, new Color(0, 255, 0))
    };

    private static final Color BACKGROUND_COLOR = Color.WHITE;

    private static final Dimension CANVAS_DIMENSION = new Dimension(1024, 1024);
    private static final Dimension VIEWPORT_DIMENSION = new Dimension(1, 1);

    private static final double PROJECTION_PLANE_DISTANCE = 1;

    private static final Point CANVAS_MIN = new Point(-CANVAS_DIMENSION.width / 2, -CANVAS_DIMENSION.height / 2);
    private static final Point CANVAS_MAX = new Point(CANVAS_DIMENSION.width / 2, CANVAS_DIMENSION.height / 2);
    private BufferedImage image;

    public Scene1() {
        image = new BufferedImage(CANVAS_DIMENSION.width, CANVAS_DIMENSION.height, BufferedImage.TYPE_INT_RGB);
        render();
    }

    private void putPixel(BufferedImage image, int x, int y, int color) {
        int targetX = x - CANVAS_MIN.x;
        int targetY = CANVAS_DIMENSION.height - (y - CANVAS_MIN.y) - 1;
        image.setRGB(targetX, targetY, color);
    }

    private void render() {
        Dot camera = new Dot(0, 0, 0);
        for (int y = CANVAS_MIN.y; y < CANVAS_MAX.y; y++) {
            for (int x = CANVAS_MIN.x; x < CANVAS_MAX.x; x++) {
                Dot viewPort = canvasToViewPort(x, y);
                Color color = traceRay(camera, viewPort, 1, Double.MAX_VALUE);
                putPixel(image, x, y, color.getRGB());
            }
        }

        repaint();
    }

    private Dot canvasToViewPort(double x, double y) {
        return new Dot(
                x * VIEWPORT_DIMENSION.width / CANVAS_DIMENSION.width,
                y * VIEWPORT_DIMENSION.height / CANVAS_DIMENSION.height,
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
            return BACKGROUND_COLOR;
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

    @Override
    public void paint(Graphics graphics) {
        if (Objects.requireNonNull(graphics) instanceof Graphics2D g) {
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, CANVAS_DIMENSION.width, CANVAS_DIMENSION.height);
            g.drawImage(image, 0, 0, null);
        }
    }
}
