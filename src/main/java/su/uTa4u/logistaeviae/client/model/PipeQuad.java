package su.uTa4u.logistaeviae.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;

// four points in order
// 3 2
// 0 1
public final class PipeQuad {
    public static final int VERTEX_COUNT = 4;
    public static final int POS_COUNT = 3;
    public static final int VERTEX_LENGHT = 5; // x, y, z, u, v
    public static final int INDEX_COUNT = 6;

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

    public BakedQuad bake(EnumFacing dir) {
        final VertexFormat format = DefaultVertexFormats.ITEM;
        int size = format.getSize() / VERTEX_COUNT;
        int[] vertexData = new int[VERTEX_COUNT * size];
        for (int i = 0; i < VERTEX_COUNT; i++) {
            vertexData[size * i + 0] = Float.floatToRawIntBits(this.xs[i]);
            vertexData[size * i + 1] = Float.floatToRawIntBits(this.ys[i]);
            vertexData[size * i + 2] = Float.floatToRawIntBits(this.zs[i]);
            vertexData[size * i + 3] = 0xFFFFFFFF;
            vertexData[size * i + 4] = Float.floatToRawIntBits(this.us[i]);
            vertexData[size * i + 5] = Float.floatToRawIntBits(this.vs[i]);
//            vertexData[size * i + 6] = 0xFFFFFFFF;
            ForgeHooksClient.fillNormal(vertexData, dir);
        }
        return new BakedQuad(
                vertexData,
                -1,
                dir,
                Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite(),
                false,
                format
        );
    }

    public static PipeQuad withSamePos(PipeQuad quad) {
        return new PipeQuad(
                quad.xs[0], quad.ys[0], quad.zs[0],
                quad.xs[1], quad.ys[1], quad.zs[1],
                quad.xs[2], quad.ys[2], quad.zs[2],
                quad.xs[3], quad.ys[3], quad.zs[3]
        );
    }
}
