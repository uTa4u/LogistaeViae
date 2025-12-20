package su.uTa4u.logistaeviae.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import su.uTa4u.logistaeviae.Tags;
import su.uTa4u.logistaeviae.tileentity.TileEntitySimplePipe;

import java.util.EnumMap;

public final class PipeModelManager {
    private static final float FROM = 0.25f;
    private static final float TOOO = 0.75f;

    public static final ResourceLocation TEXTURE_BASE = new ResourceLocation(Tags.MOD_ID, "block/pipe_base");

    public static EnumMap<EnumFacing, Quad> getModelForPipe(TileEntitySimplePipe pipe) {
        EnumMap<EnumFacing, Quad> center = getCenter();
        pipe.forEachConnection((connection) -> {
            switch (connection) {
                case DOWN:
                    // X and Z axis quads extend down, down quad is removed
                    center.remove(EnumFacing.DOWN);
                    break;
                case UP:
                    // X and Z axis quads extend up, up quad is removed
                    center.remove(EnumFacing.UP);
                    break;
                case NORTH:
                    // Y and X axis quads extend north, north quad is removed
                    center.remove(EnumFacing.NORTH);
                    break;
                case SOUTH:
                    // Y and X axis quads extend south, south quad is removed
                    center.remove(EnumFacing.SOUTH);
                    break;
                case WEST:
                    // Y and Z axis quads extend west, west quad is removed
                    center.remove(EnumFacing.WEST);
                    break;
                case EAST:
                    // Y and Z axis quads extend east, east quad is removed
                    center.remove(EnumFacing.EAST);
                    break;
            }
        });
        return center;
    }

    public static EnumMap<EnumFacing, Quad> getCenter() {
        TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TEXTURE_BASE.toString());
        float umin = tex.getInterpolatedU(4);
        float umax = tex.getInterpolatedU(12);
        float vmin = tex.getInterpolatedV(12);
        float vmax = tex.getInterpolatedV(4);
        EnumMap<EnumFacing, Quad> model = new EnumMap<>(EnumFacing.class);
        model.put(EnumFacing.DOWN,
                new Quad(
                        FROM, FROM, FROM, umin, vmin,
                        TOOO, FROM, FROM, umax, vmin,
                        TOOO, FROM, TOOO, umax, vmax,
                        FROM, FROM, TOOO, umin, vmax
                )
        );
        model.put(EnumFacing.UP,
                new Quad(
                        FROM, TOOO, FROM, umin, vmax,
                        TOOO, TOOO, FROM, umax, vmax,
                        TOOO, TOOO, TOOO, umax, vmin,
                        FROM, TOOO, TOOO, umin, vmin
                )
        );
        model.put(EnumFacing.NORTH,
                new Quad(
                        TOOO, FROM, FROM, umin, vmax,
                        FROM, FROM, FROM, umax, vmax,
                        FROM, TOOO, FROM, umax, vmin,
                        TOOO, TOOO, FROM, umin, vmin
                )
        );
        model.put(EnumFacing.SOUTH,
                new Quad(
                        TOOO, FROM, TOOO, umin, vmin,
                        FROM, FROM, TOOO, umax, vmin,
                        FROM, TOOO, TOOO, umax, vmax,
                        TOOO, TOOO, TOOO, umin, vmax
                )
        );
        model.put(EnumFacing.WEST,
                new Quad(
                        FROM, FROM, FROM, umin, vmin,
                        FROM, FROM, TOOO, umax, vmin,
                        FROM, TOOO, TOOO, umax, vmax,
                        FROM, TOOO, FROM, umin, vmax
                )
        );
        model.put(EnumFacing.EAST,
                new Quad(
                        TOOO, FROM, FROM, umin, vmax,
                        TOOO, FROM, TOOO, umax, vmax,
                        TOOO, TOOO, TOOO, umax, vmin,
                        TOOO, TOOO, FROM, umin, vmin
                )
        );
        return model;
    }

    private PipeModelManager() {
    }
}
