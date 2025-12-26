package su.uTa4u.logistaeviae.client.model;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import su.uTa4u.logistaeviae.block.BlockPipe;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import java.util.EnumMap;

public final class PipeModelManager {
    public static final int BASE_INSTANCE_COUNT = 64;
    public static final int QUAD_COUNT = 6;

    private static final float FROM = 0.25f;
    private static final float TOOO = 0.75f;

    // ArrayMap implementation should be fine for only 64 entries
    private static final Byte2ObjectMap<EnumMap<EnumFacing, Quad>> CACHE = new Byte2ObjectArrayMap<>();

    public static EnumMap<EnumFacing, Quad> getQuadsForPipe(TileEntityPipe pipe) {
        byte packedConnections = pipe.packConnections();
        if (!CACHE.containsKey(packedConnections)) {
            CACHE.put(packedConnections, computeQuadsForPipe(pipe));
        }

        Block block = pipe.getWorld().getBlockState(pipe.getPos()).getBlock();
        if (!(block instanceof BlockPipe)) throw new RuntimeException("TileEntityPipe is not BlockPipe, WTF");
        TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(((BlockPipe) block).getTexture().toString());

        EnumMap<EnumFacing, Quad> model = CACHE.get(packedConnections);
        texture(model, tex);
        return model;
    }

    private static EnumMap<EnumFacing, Quad> computeQuadsForPipe(TileEntityPipe pipe) {
        EnumMap<EnumFacing, Quad> model = getCenter();
        pipe.forEachConnection((connection) -> {
            Quad quad;
            switch (connection) {
                case DOWN: {
                    // X and Z axis quads extend down
                    quad = model.get(EnumFacing.NORTH);
                    quad.ys[0] = 0.0f;
                    quad.ys[1] = 0.0f;
                    quad = model.get(EnumFacing.SOUTH);
                    quad.ys[0] = 0.0f;
                    quad.ys[1] = 0.0f;
                    quad = model.get(EnumFacing.WEST);
                    quad.ys[0] = 0.0f;
                    quad.ys[1] = 0.0f;
                    quad = model.get(EnumFacing.EAST);
                    quad.ys[0] = 0.0f;
                    quad.ys[1] = 0.0f;
                    break;
                }
                case UP: {
                    // X and Z axis quads extend
                    quad = model.get(EnumFacing.NORTH);
                    quad.ys[2] = 1.0f;
                    quad.ys[3] = 1.0f;
                    quad = model.get(EnumFacing.SOUTH);
                    quad.ys[2] = 1.0f;
                    quad.ys[3] = 1.0f;
                    quad = model.get(EnumFacing.WEST);
                    quad.ys[2] = 1.0f;
                    quad.ys[3] = 1.0f;
                    quad = model.get(EnumFacing.EAST);
                    quad.ys[2] = 1.0f;
                    quad.ys[3] = 1.0f;
                    break;
                }
                case NORTH: {
                    // Y and X axis quads extend north
                    quad = model.get(EnumFacing.DOWN);
                    quad.zs[0] = 0.0f;
                    quad.zs[1] = 0.0f;
                    quad = model.get(EnumFacing.UP);
                    quad.zs[0] = 0.0f;
                    quad.zs[1] = 0.0f;
                    quad = model.get(EnumFacing.WEST);
                    quad.zs[0] = 0.0f;
                    quad.zs[3] = 0.0f;
                    quad = model.get(EnumFacing.EAST);
                    quad.zs[1] = 0.0f;
                    quad.zs[2] = 0.0f;
                    break;
                }
                case SOUTH: {
                    // Y and X axis quads extend south
                    quad = model.get(EnumFacing.DOWN);
                    quad.zs[2] = 1.0f;
                    quad.zs[3] = 1.0f;
                    quad = model.get(EnumFacing.UP);
                    quad.zs[2] = 1.0f;
                    quad.zs[3] = 1.0f;
                    quad = model.get(EnumFacing.WEST);
                    quad.zs[1] = 1.0f;
                    quad.zs[2] = 1.0f;
                    quad = model.get(EnumFacing.EAST);
                    quad.zs[0] = 1.0f;
                    quad.zs[3] = 1.0f;
                    break;
                }
                case WEST: {
                    // Y and Z axis quads extend west
                    quad = model.get(EnumFacing.DOWN);
                    quad.xs[0] = 0.0f;
                    quad.xs[3] = 0.0f;
                    quad = model.get(EnumFacing.UP);
                    quad.xs[1] = 0.0f;
                    quad.xs[2] = 0.0f;
                    quad = model.get(EnumFacing.NORTH);
                    quad.xs[1] = 0.0f;
                    quad.xs[2] = 0.0f;
                    quad = model.get(EnumFacing.SOUTH);
                    quad.xs[0] = 0.0f;
                    quad.xs[3] = 0.0f;
                    break;
                }
                case EAST: {
                    // Y and Z axis quads extend east
                    quad = model.get(EnumFacing.DOWN);
                    quad.xs[1] = 1.0f;
                    quad.xs[2] = 1.0f;
                    quad = model.get(EnumFacing.UP);
                    quad.xs[0] = 1.0f;
                    quad.xs[3] = 1.0f;
                    quad = model.get(EnumFacing.NORTH);
                    quad.xs[0] = 1.0f;
                    quad.xs[3] = 1.0f;
                    quad = model.get(EnumFacing.SOUTH);
                    quad.xs[1] = 1.0f;
                    quad.xs[2] = 1.0f;
                    break;
                }
            }
        });
        return model;
    }

    private static EnumMap<EnumFacing, Quad> getCenter() {
        EnumMap<EnumFacing, Quad> quads = new EnumMap<>(EnumFacing.class);
        quads.put(EnumFacing.DOWN,
                new Quad(
                        FROM, FROM, FROM,
                        TOOO, FROM, FROM,
                        TOOO, FROM, TOOO,
                        FROM, FROM, TOOO
                )
        );
        quads.put(EnumFacing.UP,
                new Quad(
                        TOOO, TOOO, FROM,
                        FROM, TOOO, FROM,
                        FROM, TOOO, TOOO,
                        TOOO, TOOO, TOOO
                )
        );
        quads.put(EnumFacing.NORTH,
                new Quad(
                        TOOO, FROM, FROM,
                        FROM, FROM, FROM,
                        FROM, TOOO, FROM,
                        TOOO, TOOO, FROM
                )
        );
        quads.put(EnumFacing.SOUTH,
                new Quad(
                        FROM, FROM, TOOO,
                        TOOO, FROM, TOOO,
                        TOOO, TOOO, TOOO,
                        FROM, TOOO, TOOO
                )
        );
        quads.put(EnumFacing.WEST,
                new Quad(
                        FROM, FROM, FROM,
                        FROM, FROM, TOOO,
                        FROM, TOOO, TOOO,
                        FROM, TOOO, FROM
                )
        );
        quads.put(EnumFacing.EAST,
                new Quad(
                        TOOO, FROM, TOOO,
                        TOOO, FROM, FROM,
                        TOOO, TOOO, FROM,
                        TOOO, TOOO, TOOO
                )
        );
        return quads;
    }

    // These are not technically correct UV coords, some are flipped
    private static void texture(EnumMap<EnumFacing, Quad> model, TextureAtlasSprite tex) {
        float umin;
        float umax;
        float vmin;
        float vmax;
        for (EnumFacing dir : EnumFacing.VALUES) {
            Quad quad = model.get(dir);
            switch (dir) {
                case DOWN:
                    umin = tex.getInterpolatedU(16 * (1 - quad.xs[0]));
                    umax = tex.getInterpolatedU(16 * (1 - quad.xs[1]));
                    vmin = tex.getInterpolatedV(16 * quad.zs[0]);
                    vmax = tex.getInterpolatedV(16 * quad.zs[3]);
                    break;
                case UP:
                    umin = tex.getInterpolatedU(16 * quad.xs[0]);
                    umax = tex.getInterpolatedU(16 * quad.xs[1]);
                    vmin = tex.getInterpolatedV(16 * quad.zs[0]);
                    vmax = tex.getInterpolatedV(16 * quad.zs[3]);
                    break;
                case NORTH:
                    umin = tex.getInterpolatedU(16 * (1 - quad.xs[0]));
                    umax = tex.getInterpolatedU(16 * (1 - quad.xs[1]));
                    vmin = tex.getInterpolatedV(16 * quad.ys[0]);
                    vmax = tex.getInterpolatedV(16 * quad.ys[3]);
                    break;
                case SOUTH:
                    umin = tex.getInterpolatedU(16 * quad.xs[0]);
                    umax = tex.getInterpolatedU(16 * quad.xs[1]);
                    vmin = tex.getInterpolatedV(16 * quad.ys[0]);
                    vmax = tex.getInterpolatedV(16 * quad.ys[3]);
                    break;
                case WEST:
                    umin = tex.getInterpolatedU(16 * (1 - quad.zs[0]));
                    umax = tex.getInterpolatedU(16 * (1 - quad.zs[1]));
                    vmin = tex.getInterpolatedV(16 * quad.ys[0]);
                    vmax = tex.getInterpolatedV(16 * quad.ys[3]);
                    break;
                case EAST:
                    umin = tex.getInterpolatedU(16 * quad.zs[0]);
                    umax = tex.getInterpolatedU(16 * quad.zs[1]);
                    vmin = tex.getInterpolatedV(16 * quad.ys[0]);
                    vmax = tex.getInterpolatedV(16 * quad.ys[3]);
                    break;
                default: throw new IllegalStateException("Unknown EnumFacing value!");
            }
            quad.texture(
                    umin, vmin,
                    umax, vmin,
                    umax, vmax,
                    umin, vmax
            );
        }
    }

    private PipeModelManager() {
    }
}
