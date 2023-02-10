package fi.misaki.raytrace.light;

import fi.misaki.raytrace.render.Point3D;
import fi.misaki.raytrace.render.Vector3D;

import java.awt.*;
import java.util.function.Function;

public class DirectionalLight implements Light {
    private Color tint;
    private Vector3D direction;

    public DirectionalLight(Color tint, Vector3D direction) {
        this.tint = tint;
        this.direction = direction;
    }

    public Color getTint(
            Function<Vector3D, Color> getShadowTint,
            Point3D target,
            Vector3D normal,
            Vector3D toViewPort,
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
