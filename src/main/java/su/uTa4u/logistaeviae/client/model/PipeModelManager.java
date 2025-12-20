package su.uTa4u.logistaeviae.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import su.uTa4u.logistaeviae.Tags;

public final class PipeModelManager {
    private static final float FROM = 0.25f;
    private static final float TOOO = 0.75f;

    public static final ResourceLocation TEXTURE_BASE = new ResourceLocation(Tags.MOD_ID, "block/pipe_base");

    // DOWN - UP - NORTH - SOUTH - WEST - EAST
    public static Quad[] getCenter() {
        TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TEXTURE_BASE.toString());
        return new Quad[]{
                new Quad(
                        FROM, FROM, FROM,
                        TOOO, FROM, FROM,
                        TOOO, FROM, TOOO,
                        FROM, FROM, TOOO,
                        tex
                ),
                new Quad(
                        FROM, TOOO, FROM,
                        TOOO, TOOO, FROM,
                        TOOO, TOOO, TOOO,
                        FROM, TOOO, TOOO,
                        tex
                ),
                new Quad(
                        TOOO, FROM, FROM,
                        FROM, FROM, FROM,
                        FROM, TOOO, FROM,
                        TOOO, TOOO, FROM,
                        tex
                ),
                new Quad(
                        TOOO, FROM, TOOO,
                        FROM, FROM, TOOO,
                        FROM, TOOO, TOOO,
                        TOOO, TOOO, TOOO,
                        tex
                ),
                new Quad(
                        FROM, FROM, FROM,
                        FROM, FROM, TOOO,
                        FROM, TOOO, TOOO,
                        FROM, TOOO, FROM,
                        tex
                ),
                new Quad(
                        TOOO, FROM, FROM,
                        TOOO, FROM, TOOO,
                        TOOO, TOOO, TOOO,
                        TOOO, TOOO, FROM,
                        tex
                ),
        };
    }

    private PipeModelManager() {
    }
}
