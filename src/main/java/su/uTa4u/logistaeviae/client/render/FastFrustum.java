package su.uTa4u.logistaeviae.client.render;

import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.util.math.Vec3i;

// a failed experiment
// FIXME: try fixing this, it's the most expensive part of rendering outside of opengl calls, also try profiling rendering with System.nanotime
public final class FastFrustum {
    private final ClippingHelperImpl clippingHelper;
    private double x;
    private double y;
    private double z;

    public FastFrustum() {
        this.clippingHelper = new ClippingHelperImpl();
        this.clippingHelper.init();
    }

    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isCubeVisible(double x, double y, double z, int size) {
        float minX = (float) (x - this.x);
        float minY = (float) (y - this.y);
        float minZ = (float) (z - this.z);
        float maxX = minX + size;
        float maxY = minY + size;
        float maxZ = minZ + size;
        for (float[] plane : this.clippingHelper.frustum) {
            // normal xyz | dist
            float centerX = (maxX + minX) * 0.5f;
            float centerY = (maxY + minY) * 0.5f;
            float centerZ = (maxZ + minZ) * 0.5f;
            float extentX = maxX - centerX;
            float extentY = maxY - centerY;
            float extentZ = maxZ - centerZ;

            float r = extentX * Math.abs(plane[0]) + extentY * Math.abs(plane[1]) + extentZ * Math.abs(plane[2]);

            float s = plane[0] * centerX + plane[1] * centerY + plane[2] * centerZ - plane[3];

            if (Math.abs(s) <= r) {
                return false;
            }
        }
        return true;
    }

    public boolean isChunkVisible(Vec3i pos) {
        return this.isCubeVisible(pos.getX() << 4, pos.getY() << 4, pos.getZ() << 4, 16);
    }

    public boolean isBlockVisible(Vec3i pos) {
        return this.isCubeVisible(pos.getX(), pos.getY(), pos.getZ(), 1);
    }

}
