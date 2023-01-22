package fi.misaki.raytrace;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class Main extends JPanel {

    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Dimension CANVAS_DIMENSION = new Dimension(800, 600);

    private static final Scene scene = new Scene(
            BACKGROUND_COLOR,
            CANVAS_DIMENSION
    );

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
        doubleBuffer = scene.render();
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
