package fi.misaki.raytrace.light;

import java.awt.*;

public class AmbientLight implements Light {
    private Color tint;

    public AmbientLight(Color tint) {
        this.tint = tint;
    }

    public Color getTint() {
        return tint;
    }
}
