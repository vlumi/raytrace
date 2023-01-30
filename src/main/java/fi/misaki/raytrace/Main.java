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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class Main extends JPanel implements ComponentListener {

    // TODO: get from configuration file
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Shape[] SHAPES = {
            new Sphere(new Point3D(0, -1, 3), 1, new Color(255, 0, 0), 500, 0.2),
            new Sphere(new Point3D(2, 0, 4), 1, new Color(0, 0, 255), 500, 0.3),
            new Sphere(new Point3D(-2, 0, 4), 1, new Color(0, 255, 0), 10, 0.4),
            new Sphere(new Point3D(0, -5001, 0), 5000, new Color(255, 255, 0), 1000, 0.5)
    };
    private static final Light[] LIGHTS = {
            new AmbientLight(new Color(51, 51, 51)),
            new PointLight(new Color(153, 153, 153), new Point3D(2, 1, 0)),
            new DirectionalLight(new Color(51, 51, 51), new Point3D(1, 4, 4))
    };
    private static final double PROJECTION_PLANE_DISTANCE = 1;
    private static final int DEFAULT_CANVAS_WIDTH = 800;
    private static final int DEFAULT_CANVAS_HEIGHT = 600;
    private static double FOV_SCALE = 1;
    private static int RECURSION_DEPTH = 3;
    private static final Scene scene = new Scene(
            BACKGROUND_COLOR,
            SHAPES,
            LIGHTS,
            PROJECTION_PLANE_DISTANCE,
            FOV_SCALE,
            RECURSION_DEPTH
    );
    private Dimension canvasDimension;
    private BufferedImage doubleBuffer;

    public Main() {
        canvasDimension = new Dimension(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT);
        doubleBuffer = scene.render(canvasDimension);
    }

    public static void main(String... args) {
        SwingUtilities.invokeLater(() -> {
            Main self = new Main();
            JFrame frame = new JFrame("Raytrace");
            frame.add(self);
            frame.setSize(DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.addComponentListener(self);
        });
    }

    @Override
    public void paint(Graphics graphics) {
        if (Objects.requireNonNull(graphics) instanceof Graphics2D g) {
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0, 0, canvasDimension.width, canvasDimension.height);
            if (doubleBuffer != null) {
                g.drawImage(doubleBuffer, 0, 0, null);
            }
        }
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        Dimension newSize = e.getComponent().getBounds().getSize();
        if (!newSize.equals(canvasDimension)) {
            doubleBuffer = scene.render(newSize);
            repaint();
        }
        canvasDimension = newSize;
    }
}
