package fi.misaki.raytrace;

import fi.misaki.raytrace.light.AmbientLight;
import fi.misaki.raytrace.light.DirectionalLight;
import fi.misaki.raytrace.light.Light;
import fi.misaki.raytrace.light.PointLight;
import fi.misaki.raytrace.render.Point3D;
import fi.misaki.raytrace.render.Scene;
import fi.misaki.raytrace.shape.Shape;
import fi.misaki.raytrace.shape.Sphere;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class Main extends JPanel {

    // TODO: get from configuration file
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Shape[] SHAPES = {
            new Sphere(new Point3D(0, -1, 3), 1, new Color(255, 0, 0), 500),
            new Sphere(new Point3D(2, 0, 4), 1, new Color(0, 0, 255), 500),
            new Sphere(new Point3D(-2, 0, 4), 1, new Color(0, 255, 0), 10),
            new Sphere(new Point3D(0, -5001, 0), 5000, new Color(255, 255, 0), 1000)
    };
    private static final Light[] LIGHTS = {
            new AmbientLight(0.2),
            new PointLight(0.6, new Point3D(2, 1, 0)),
            new DirectionalLight(0.2, new Point3D(1, 4, 4))
    };
    private static final double PROJECTION_PLANE_DISTANCE = 1;
    private static double FOV_SCALE = 1;
    private static final Dimension CANVAS_DIMENSION = new Dimension(800, 600);

    private static final Scene scene = new Scene(BACKGROUND_COLOR, SHAPES, LIGHTS, PROJECTION_PLANE_DISTANCE, FOV_SCALE);

    private BufferedImage doubleBuffer;

    public static void main(String... args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Raytrace");
            frame.add(new Main());
            frame.setSize(CANVAS_DIMENSION.width, CANVAS_DIMENSION.height);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }

    public Main() {
        doubleBuffer = scene.render(CANVAS_DIMENSION);
    }

    @Override
    public void paint(Graphics graphics) {
        if (Objects.requireNonNull(graphics) instanceof Graphics2D g) {
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, CANVAS_DIMENSION.width, CANVAS_DIMENSION.height);
            if (doubleBuffer != null) {
                g.drawImage(doubleBuffer, 0, 0, null);
            }
        }
    }

}
