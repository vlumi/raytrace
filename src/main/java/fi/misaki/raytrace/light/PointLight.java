package fi.misaki.raytrace.light;

import fi.misaki.raytrace.render.Point3D;
import fi.misaki.raytrace.render.Vector3D;

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
            Function<Vector3D, Color> getShadowTint,
            Point3D target,
            Vector3D normal,
            Vector3D toViewPort,
            int specular
    ) {
        Vector3D direction = new Vector3D(target, position);;
        Color shadedTint = Light.applyLight(tint, getShadowTint.apply(direction));
        double angle = normal.dot(direction);
        return Light.mix(
                Light.getDiffuseIntensity(shadedTint, normal, angle, direction),
                Light.getSpecularIntensity(shadedTint, normal, toViewPort, specular, direction)
        );
    }

}
