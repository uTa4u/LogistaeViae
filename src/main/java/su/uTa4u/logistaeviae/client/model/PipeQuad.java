package su.uTa4u.logistaeviae.client.model;

// four points in order
// 3 2
// 0 1
public final class PipeQuad {
    public static final int VERTEX_COUNT = 4;
    public static final int POS_COUNT = 3;
    public static final int VERTEX_LENGHT = 5; // x, y, z, u, v
    public static final int[] INDICES = new int[]{0, 1, 3, 3, 1, 2};

    public final float[] xs;
    public final float[] ys;
    public final float[] zs;
    public final float[] us;
    public final float[] vs;

    PipeQuad(
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3
    ) {
        this.xs = new float[]{x0, x1, x2, x3};
        this.ys = new float[]{y0, y1, y2, y3};
        this.zs = new float[]{z0, z1, z2, z3};
        this.us = new float[4];
        this.vs = new float[4];
    }

    public void texture(
            float u0, float v0,
            float u1, float v1,
            float u2, float v2,
            float u3, float v3
    ) {
        this.us[0] = u0;
        this.us[1] = u1;
        this.us[2] = u2;
        this.us[3] = u3;
        this.vs[0] = v0;
        this.vs[1] = v1;
        this.vs[2] = v2;
        this.vs[3] = v3;
    }
}
