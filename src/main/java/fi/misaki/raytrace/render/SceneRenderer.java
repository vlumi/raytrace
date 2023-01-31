package fi.misaki.raytrace.render;

import fi.misaki.raytrace.scene.Scene;
import fi.misaki.raytrace.shape.Shape;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SceneRenderer {

    private final Scene scene;
    private final int recursionDepth;

    public SceneRenderer(
            Scene scene,
            int recursionDepth
    ) {
        this.scene = scene;
        this.recursionDepth = recursionDepth;
    }

    public BufferedImage render(Dimension canvasDimension) {
        Point3D camera = new Point3D(0, 0, 0);
        BufferedImage image = new BufferedImage(canvasDimension.width, canvasDimension.height, BufferedImage.TYPE_INT_RGB);
        Point canvasMin = new Point(-canvasDimension.width / 2, -canvasDimension.height / 2);
        Point canvasMax = new Point(canvasDimension.width / 2, canvasDimension.height / 2);

        for (int y = canvasMin.y; y < canvasMax.y; y++) {
            for (int x = canvasMin.x; x < canvasMax.x; x++) {
                Point3D viewPort = canvasToViewPort(canvasDimension, x, y);
                Color color = Shape.traceRay(scene, camera, viewPort, 1, Double.MAX_VALUE, scene.backgroundColor(), recursionDepth);

                int targetX = x - canvasMin.x;
                int targetY = canvasDimension.height - (y - canvasMin.y) - 1;
                putPixel(image, targetX, targetY, color.getRGB());
            }
        }
        return image;
    }

    private Point3D canvasToViewPort(Dimension canvasDimension, double x, double y) {
        DoubleDimension viewportDimension = new DoubleDimension(scene.fovScale(), scene.fovScale() * canvasDimension.height / canvasDimension.width);
        return new Point3D(
                x * viewportDimension.width() / canvasDimension.width,
                y * viewportDimension.height() / canvasDimension.height,
                scene.projectionPlaneDistance()
        );
    }

    private void putPixel(BufferedImage image, int x, int y, int color) {
        image.setRGB(x, y, color);
    }
}
