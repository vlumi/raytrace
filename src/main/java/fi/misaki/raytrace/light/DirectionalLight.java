package fi.misaki.raytrace.light;

import fi.misaki.raytrace.render.Point3D;

import java.awt.*;
import java.util.function.BiFunction;

public class DirectionalLight implements Light {
    private Color tint;
    private Point3D direction;

    public DirectionalLight(Color tint, Point3D direction) {
        this.tint = tint;
        this.direction = direction;
    }

    public Color getTint(BiFunction<Point3D, Point3D, Boolean> isInShadow,
                         Point3D target,
                         Point3D normal,
                         Point3D toViewPort,
                         int specular) {
        if (isInShadow.apply(target, direction)) {
            return Color.BLACK;
        }
        double angle = normal.dot(direction);
        return Light.mix(
                Light.getDiffuseIntensity(tint, normal, angle, direction),
                Light.getSpecularIntensity(tint, normal, toViewPort, specular, direction)
        );
    }

}
