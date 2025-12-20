package su.uTa4u.logistaeviae.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import su.uTa4u.logistaeviae.Tags;

public final class PipeModelManager {
    private static final float FROM = 0.25f;
    private static final float TOOO = 0.75f;

    public static final ResourceLocation TEXTURE_BASE = new ResourceLocation(Tags.MOD_ID, "block/pipe_base");

    public static Quad[] getCenter() {
        TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TEXTURE_BASE.toString());
        float umin = tex.getInterpolatedU(4);
        float umax = tex.getInterpolatedU(12);
        float vmin = tex.getInterpolatedV(12);
        float vmax = tex.getInterpolatedV(4);
        return new Quad[]{
                // DOWN
                new Quad(
                        FROM, FROM, FROM, umin, vmin,
                        TOOO, FROM, FROM, umax, vmin,
                        TOOO, FROM, TOOO, umax, vmax,
                        FROM, FROM, TOOO, umin, vmax
                ),
                // UP
                new Quad(
                        FROM, TOOO, FROM, umin, vmax,
                        TOOO, TOOO, FROM, umax, vmax,
                        TOOO, TOOO, TOOO, umax, vmin,
                        FROM, TOOO, TOOO, umin, vmin
                ),
                // NORTH
                new Quad(
                        TOOO, FROM, FROM, umin, vmax,
                        FROM, FROM, FROM, umax, vmax,
                        FROM, TOOO, FROM, umax, vmin,
                        TOOO, TOOO, FROM, umin, vmin
                ),
                // SOUTH
                new Quad(
                        TOOO, FROM, TOOO, umin, vmin,
                        FROM, FROM, TOOO, umax, vmin,
                        FROM, TOOO, TOOO, umax, vmax,
                        TOOO, TOOO, TOOO, umin, vmax
                ),
                // WEST
                new Quad(
                        FROM, FROM, FROM, umin, vmin,
                        FROM, FROM, TOOO, umax, vmin,
                        FROM, TOOO, TOOO, umax, vmax,
                        FROM, TOOO, FROM, umin, vmax
                ),
                // EAST
                new Quad(
                        TOOO, FROM, FROM, umin, vmax,
                        TOOO, FROM, TOOO, umax, vmax,
                        TOOO, TOOO, TOOO, umax, vmin,
                        TOOO, TOOO, FROM, umin, vmin
                ),
        };
    }

    private PipeModelManager() {
    }
}
