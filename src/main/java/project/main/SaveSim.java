package project.main;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

public class SaveSim implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    float[] u, v, p, m;

    public SaveSim() {
        this.u = Arrays.copyOf(CanvasScene.fluid.u, CanvasScene.fluid.u.length);
        this.v = Arrays.copyOf(CanvasScene.fluid.v, CanvasScene.fluid.v.length);
        this.p = Arrays.copyOf(CanvasScene.fluid.p, CanvasScene.fluid.p.length);
        this.m = Arrays.copyOf(CanvasScene.fluid.m, CanvasScene.fluid.m.length);
    }
}
