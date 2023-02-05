package fi.misaki.raytrace.light;

import fi.misaki.raytrace.render.Point3D;

import java.awt.*;
import java.util.function.Function;

public class DirectionalLight implements Light {
    private Color tint;
    private Point3D direction;

    public DirectionalLight(Color tint, Point3D direction) {
        this.tint = tint;
        this.direction = direction;
    }

    public Color getTint(
            Function<Point3D, Color> getShadowTint,
            Point3D target,
            Point3D normal,
            Point3D toViewPort,
            int specular
    ) {
        Color shadedTint = Light.applyLight(tint, getShadowTint.apply(direction));
        double angle = normal.dot(direction);
        return Light.mix(
                Light.getDiffuseIntensity(shadedTint, normal, angle, direction),
                Light.getSpecularIntensity(shadedTint, normal, toViewPort, specular, direction)
        );
    }

}
