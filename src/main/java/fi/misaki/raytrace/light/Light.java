package fi.misaki.raytrace.light;

import fi.misaki.raytrace.render.DistanceRange;
import fi.misaki.raytrace.render.Point3D;
import fi.misaki.raytrace.render.Vector3D;
import fi.misaki.raytrace.scene.Scene;
import fi.misaki.raytrace.shape.Shape;

import java.awt.*;
import java.util.Arrays;

public interface Light {
    static Color compute(Scene scene, Point3D target, Vector3D normal, Vector3D toViewPort, int specular) {
        return Arrays.stream(scene.lights())
                .map(light ->
                        switch (light) {
                            case AmbientLight l -> l.getTint();
                            case DirectionalLight l -> l.getTint(
                                    (direction) -> getShadowTint(scene.shapes(), target, direction),
                                    target,
                                    normal,
                                    toViewPort,
                                    specular
                            );
                            case PointLight l -> l.getTint(
                                    (direction) -> getShadowTint(scene.shapes(), target, direction),
                                    target,
                                    normal,
                                    toViewPort,
                                    specular
                            );
                            default -> Color.BLACK;
                        }
                )
                .reduce(Color.BLACK, Light::mix);
    }

    private static Color getShadowTint(Shape[] shapes, Point3D target, Vector3D lightDirection) {
        // TODO: calculate shadow color
        if (isIntersectingShape(shapes, target, lightDirection, new DistanceRange(1))) {
            return Color.BLACK;
        }
        return new Color(255, 255, 255);
    }

    public static boolean isIntersectingShape(Shape[] shapes, Point3D origin, Vector3D direction, DistanceRange range) {
        return Arrays.stream(shapes)
                .anyMatch(shape -> shape.isIntersecting(origin, direction, range));
    }

    static Color getDiffuseIntensity(Color tint, Vector3D normal, double angle, Vector3D direction) {
        return angle > 0
                ? applyLight(tint, angle / (normal.length() * direction.length()))
                : Color.BLACK;
    }

    static Color getSpecularIntensity(Color tint, Vector3D normal, Vector3D toViewPort, int specular, Vector3D direction) {
        if (specular <= 0) {
            return Color.BLACK;
        }
        Vector3D reflectedDirection = getReflectedDirection(normal, direction);
        double angle = reflectedDirection.dot(toViewPort);
        return angle > 0 ?
                applyLight(tint, Math.pow(angle / (reflectedDirection.length() * toViewPort.length()), specular))
                : Color.BLACK;
    }

    static Vector3D getReflectedDirection(Vector3D normal, Vector3D direction) {
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
