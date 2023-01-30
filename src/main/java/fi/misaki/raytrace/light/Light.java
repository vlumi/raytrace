package fi.misaki.raytrace.light;

import fi.misaki.raytrace.render.Point3D;

import java.awt.*;

public interface Light {
    static Color getDiffuseIntensity(Color tint, Point3D normal, double angle, Point3D direction) {
        return angle > 0
                ? applyLight(tint, angle / (normal.length() * direction.length()))
                : Color.BLACK;
    }

    static Color getSpecularIntensity(Color tint, Point3D normal, Point3D toViewPort, int specular, Point3D direction) {
        if (specular <= 0) {
            return Color.BLACK;
        }
        Point3D reflectedDirection = getReflectedDirection(normal, direction);
        double angle = reflectedDirection.dot(toViewPort);
        return angle > 0 ?
                applyLight(tint, Math.pow(angle / (reflectedDirection.length() * toViewPort.length()), specular))
                : Color.BLACK;
    }

    static Point3D getReflectedDirection(Point3D normal, Point3D direction) {
        return normal.multiply(2).multiply(normal.dot(direction)).minus(direction);
    }

    static Color applyLight(Color target, Color light) {
        return new Color(
                applyLightChannel(target.getRed(), 1.0 * light.getRed() / 255),
                applyLightChannel(target.getGreen(), 1.0 * light.getGreen() / 255),
                applyLightChannel(target.getBlue(), 1.0 * light.getBlue() / 255)
        );
    }

    static Color applyLight(Color color, double intensity) {
        return new Color(
                applyLightChannel(color.getRed(), intensity),
                applyLightChannel(color.getGreen(), intensity),
                applyLightChannel(color.getBlue(), intensity)
        );
    }

    private static int applyLightChannel(int target, double intensity) {
        return clampColorChannel((int) Math.round(target * intensity));
    }

    static Color mix(Color a, Color b) {
        return new Color(
                clampColorChannel(a.getRed() + b.getRed()),
                clampColorChannel(a.getGreen() + b.getGreen()),
                clampColorChannel(a.getBlue() + b.getBlue())
        );
    }

    private static int clampColorChannel(int value) {
        return Math.max(0, Math.min(255, value));
    }

}
