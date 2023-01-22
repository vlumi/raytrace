package fi.misaki.raytrace;

import javax.swing.*;

public class Main extends JPanel {

    public static void main(String... args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Raytrace");
            frame.add(new Scene1());
            frame.setSize(1024, 1024);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        });
    }
}
