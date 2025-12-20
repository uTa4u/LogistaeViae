package su.uTa4u.logistaeviae.client.model;

// TODO maybe use UnpackedBakedQuad idk
// four points in order
// 3 2
// 0 1
public final class Quad {
    public final float[] xs;
    public final float[] ys;
    public final float[] zs;
    //    final int[] colors;
    public final float[] us;
    public final float[] vs;
//    final int[] skyLights;
//    final int[] blockLights;

    Quad(
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3
    ) {
        this.xs = new float[]{x0, x1, x2, x3};
        this.ys = new float[]{y0, y1, y2, y3};
        this.zs = new float[]{z0, z1, z2, z3};
        this.us = new float[]{u0, u1, u2, u3};
        this.vs = new float[]{v0, v1, v2, v3};
    }

    public void extendDown(float newV) {
        this.ys[0] = 0.0f;
        this.ys[1] = 0.0f;
        this.vs[0] = newV;
        this.vs[1] = newV;
    }

    public void extendUp(float newV) {
        this.ys[2] = 1.0f;
        this.ys[3] = 1.0f;
        this.vs[2] = newV;
        this.vs[3] = newV;
    }

    public void extendNorth(float newU) {
        this.zs[0] = 0.0f;
        this.zs[3] = 0.0f;
        this.us[0] = newU;
        this.us[3] = newU;
    }

    public void extendSouth(float newU) {
        this.zs[1] = 1.0f;
        this.zs[2] = 1.0f;
        this.us[1] = newU;
        this.us[2] = newU;
    }

    public void extendWest(float newU) {
        this.xs[1] = 0.0f;
        this.xs[2] = 0.0f;
        this.us[1] = newU;
        this.us[2] = newU;
    }

    public void extendEast(float newU) {
        this.xs[0] = 1.0f;
        this.xs[3] = 1.0f;
        this.us[0] = newU;
        this.us[3] = newU;
    }
}
