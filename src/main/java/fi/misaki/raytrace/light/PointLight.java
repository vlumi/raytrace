package fi.misaki.raytrace.light;

import fi.misaki.raytrace.render.Point3D;

import java.awt.*;
import java.util.function.Function;

public class PointLight implements Light {
    private Color tint;
    private Point3D position;

    public PointLight(Color tint, Point3D position) {
        this.tint = tint;
        this.position = position;
    }

    public Color getTint(
            Function<Point3D, Color> getShadowTint,
            Point3D target,
            Point3D normal,
            Point3D toViewPort,
            int specular
    ) {
        Point3D direction = position.minus(target);
        Color shadedTint = Light.applyLight(tint, getShadowTint.apply(direction));
        double angle = normal.dot(direction);
        return Light.mix(
                Light.getDiffuseIntensity(shadedTint, normal, angle, direction),
                Light.getSpecularIntensity(shadedTint, normal, toViewPort, specular, direction)
        );
    }

}
