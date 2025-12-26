package su.uTa4u.logistaeviae.client.model;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import su.uTa4u.logistaeviae.block.BlockPipe;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import java.util.EnumMap;

public final class PipeModelManager {
    public static final int BASE_INSTANCE_COUNT = 64;
    public static final int QUAD_COUNT = 6;

    // ArrayMap implementation should be fine for only 64 entries
    private static final Byte2ObjectMap<EnumMap<EnumFacing, PipeQuad>> CACHE = new Byte2ObjectArrayMap<>();

    public static EnumMap<EnumFacing, PipeQuad> getTexturedQuadsForPipe(TileEntityPipe pipe) {
        EnumMap<EnumFacing, PipeQuad> model = getQuadsForPipe(pipe.packConnections());

        Block block = pipe.getWorld().getBlockState(pipe.getPos()).getBlock();
        if (!(block instanceof BlockPipe)) throw new RuntimeException("TileEntityPipe is not BlockPipe, WTF");
        texture(model, ((BlockPipe) block).getTexture());

        return model;
    }

    public static EnumMap<EnumFacing, PipeQuad> getQuadsForPipe(byte packedConnections) {
        if (!CACHE.containsKey(packedConnections)) {
            CACHE.put(packedConnections, computeQuadsForPipe(packedConnections));
        }
        return CACHE.get(packedConnections);
    }

    private static EnumMap<EnumFacing, PipeQuad> computeQuadsForPipe(byte packedConnections) {
        EnumMap<EnumFacing, PipeQuad> model = getCenter();

        for (EnumFacing connection : TileEntityPipe.unpackConnections(packedConnections)) {
            PipeQuad quad;
            switch (connection) {
                case DOWN: {
                    // X and Z axis quads extend down
                    quad = model.get(EnumFacing.NORTH);
                    quad.putY(0, PipeQuad.POS_0_00);
                    quad.putY(1, PipeQuad.POS_0_00);
                    quad = model.get(EnumFacing.SOUTH);
                    quad.putY(0, PipeQuad.POS_0_00);
                    quad.putY(1, PipeQuad.POS_0_00);
                    quad = model.get(EnumFacing.WEST);
                    quad.putY(0, PipeQuad.POS_0_00);
                    quad.putY(1, PipeQuad.POS_0_00);
                    quad = model.get(EnumFacing.EAST);
                    quad.putY(0, PipeQuad.POS_0_00);
                    quad.putY(1, PipeQuad.POS_0_00);
                    break;
                }
                case UP: {
                    // X and Z axis quads extend
                    quad = model.get(EnumFacing.NORTH);
                    quad.putY(2, PipeQuad.POS_1_00);
                    quad.putY(3, PipeQuad.POS_1_00);
                    quad = model.get(EnumFacing.SOUTH);
                    quad.putY(2, PipeQuad.POS_1_00);
                    quad.putY(3, PipeQuad.POS_1_00);
                    quad = model.get(EnumFacing.WEST);
                    quad.putY(2, PipeQuad.POS_1_00);
                    quad.putY(3, PipeQuad.POS_1_00);
                    quad = model.get(EnumFacing.EAST);
                    quad.putY(2, PipeQuad.POS_1_00);
                    quad.putY(3, PipeQuad.POS_1_00);
                    break;
                }
                case NORTH: {
                    // Y and X axis quads extend north
                    quad = model.get(EnumFacing.DOWN);
                    quad.putZ(0, PipeQuad.POS_0_00);
                    quad.putZ(1, PipeQuad.POS_0_00);
                    quad = model.get(EnumFacing.UP);
                    quad.putZ(0, PipeQuad.POS_0_00);
                    quad.putZ(1, PipeQuad.POS_0_00);
                    quad = model.get(EnumFacing.WEST);
                    quad.putZ(0, PipeQuad.POS_0_00);
                    quad.putZ(3, PipeQuad.POS_0_00);
                    quad = model.get(EnumFacing.EAST);
                    quad.putZ(1, PipeQuad.POS_0_00);
                    quad.putZ(2, PipeQuad.POS_0_00);
                    break;
                }
                case SOUTH: {
                    // Y and X axis quads extend south
                    quad = model.get(EnumFacing.DOWN);
                    quad.putZ(2, PipeQuad.POS_1_00);
                    quad.putZ(3, PipeQuad.POS_1_00);
                    quad = model.get(EnumFacing.UP);
                    quad.putZ(2, PipeQuad.POS_1_00);
                    quad.putZ(3, PipeQuad.POS_1_00);
                    quad = model.get(EnumFacing.WEST);
                    quad.putZ(1, PipeQuad.POS_1_00);
                    quad.putZ(2, PipeQuad.POS_1_00);
                    quad = model.get(EnumFacing.EAST);
                    quad.putZ(0, PipeQuad.POS_1_00);
                    quad.putZ(3, PipeQuad.POS_1_00);
                    break;
                }
                case WEST: {
                    // Y and Z axis quads extend west
                    quad = model.get(EnumFacing.DOWN);
                    quad.putX(0, PipeQuad.POS_0_00);
                    quad.putX(3, PipeQuad.POS_0_00);
                    quad = model.get(EnumFacing.UP);
                    quad.putX(1, PipeQuad.POS_0_00);
                    quad.putX(2, PipeQuad.POS_0_00);
                    quad = model.get(EnumFacing.NORTH);
                    quad.putX(1, PipeQuad.POS_0_00);
                    quad.putX(2, PipeQuad.POS_0_00);
                    quad = model.get(EnumFacing.SOUTH);
                    quad.putX(0, PipeQuad.POS_0_00);
                    quad.putX(3, PipeQuad.POS_0_00);
                    break;
                }
                case EAST: {
                    // Y and Z axis quads extend east
                    quad = model.get(EnumFacing.DOWN);
                    quad.putX(1, PipeQuad.POS_1_00);
                    quad.putX(2, PipeQuad.POS_1_00);
                    quad = model.get(EnumFacing.UP);
                    quad.putX(0, PipeQuad.POS_1_00);
                    quad.putX(3, PipeQuad.POS_1_00);
                    quad = model.get(EnumFacing.NORTH);
                    quad.putX(0, PipeQuad.POS_1_00);
                    quad.putX(3, PipeQuad.POS_1_00);
                    quad = model.get(EnumFacing.SOUTH);
                    quad.putX(1, PipeQuad.POS_1_00);
                    quad.putX(2, PipeQuad.POS_1_00);
                    break;
                }
            }
        }
        return model;
    }

    private static EnumMap<EnumFacing, PipeQuad> getCenter() {
        EnumMap<EnumFacing, PipeQuad> quads = new EnumMap<>(EnumFacing.class);
        quads.put(EnumFacing.DOWN,
                new PipeQuad(
                        PipeQuad.POS_0_25, PipeQuad.POS_0_25, PipeQuad.POS_0_25,
                        PipeQuad.POS_0_75, PipeQuad.POS_0_25, PipeQuad.POS_0_25,
                        PipeQuad.POS_0_75, PipeQuad.POS_0_25, PipeQuad.POS_0_75,
                        PipeQuad.POS_0_25, PipeQuad.POS_0_25, PipeQuad.POS_0_75
                )
        );
        quads.put(EnumFacing.UP,
                new PipeQuad(
                        PipeQuad.POS_0_75, PipeQuad.POS_0_75, PipeQuad.POS_0_25,
                        PipeQuad.POS_0_25, PipeQuad.POS_0_75, PipeQuad.POS_0_25,
                        PipeQuad.POS_0_25, PipeQuad.POS_0_75, PipeQuad.POS_0_75,
                        PipeQuad.POS_0_75, PipeQuad.POS_0_75, PipeQuad.POS_0_75
                )
        );
        quads.put(EnumFacing.NORTH,
                new PipeQuad(
                        PipeQuad.POS_0_75, PipeQuad.POS_0_25, PipeQuad.POS_0_25,
                        PipeQuad.POS_0_25, PipeQuad.POS_0_25, PipeQuad.POS_0_25,
                        PipeQuad.POS_0_25, PipeQuad.POS_0_75, PipeQuad.POS_0_25,
                        PipeQuad.POS_0_75, PipeQuad.POS_0_75, PipeQuad.POS_0_25
                )
        );
        quads.put(EnumFacing.SOUTH,
                new PipeQuad(
                        PipeQuad.POS_0_25, PipeQuad.POS_0_25, PipeQuad.POS_0_75,
                        PipeQuad.POS_0_75, PipeQuad.POS_0_25, PipeQuad.POS_0_75,
                        PipeQuad.POS_0_75, PipeQuad.POS_0_75, PipeQuad.POS_0_75,
                        PipeQuad.POS_0_25, PipeQuad.POS_0_75, PipeQuad.POS_0_75
                )
        );
        quads.put(EnumFacing.WEST,
                new PipeQuad(
                        PipeQuad.POS_0_25, PipeQuad.POS_0_25, PipeQuad.POS_0_25,
                        PipeQuad.POS_0_25, PipeQuad.POS_0_25, PipeQuad.POS_0_75,
                        PipeQuad.POS_0_25, PipeQuad.POS_0_75, PipeQuad.POS_0_75,
                        PipeQuad.POS_0_25, PipeQuad.POS_0_75, PipeQuad.POS_0_25
                )
        );
        quads.put(EnumFacing.EAST,
                new PipeQuad(
                        PipeQuad.POS_0_75, PipeQuad.POS_0_25, PipeQuad.POS_0_75,
                        PipeQuad.POS_0_75, PipeQuad.POS_0_25, PipeQuad.POS_0_25,
                        PipeQuad.POS_0_75, PipeQuad.POS_0_75, PipeQuad.POS_0_25,
                        PipeQuad.POS_0_75, PipeQuad.POS_0_75, PipeQuad.POS_0_75
                )
        );
        return quads;
    }

    // These are not technically correct UV coords, some are flipped
    private static void texture(EnumMap<EnumFacing, PipeQuad> model, ResourceLocation texLoc) {
        TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texLoc.toString());
        float umin;
        float umax;
        float vmin;
        float vmax;
        for (EnumFacing dir : EnumFacing.VALUES) {
            PipeQuad quad = model.get(dir);
            switch (dir) {
                case DOWN:
                    umin = tex.getInterpolatedU(16 * (1 - quad.getX(0)));
                    umax = tex.getInterpolatedU(16 * (1 - quad.getX(1)));
                    vmin = tex.getInterpolatedV(16 * quad.getZ(0));
                    vmax = tex.getInterpolatedV(16 * quad.getZ(3));
                    break;
                case UP:
                    umin = tex.getInterpolatedU(16 * quad.getX(0));
                    umax = tex.getInterpolatedU(16 * quad.getX(1));
                    vmin = tex.getInterpolatedV(16 * quad.getZ(0));
                    vmax = tex.getInterpolatedV(16 * quad.getZ(3));
                    break;
                case NORTH:
                    umin = tex.getInterpolatedU(16 * (1 - quad.getX(0)));
                    umax = tex.getInterpolatedU(16 * (1 - quad.getX(1)));
                    vmin = tex.getInterpolatedV(16 * quad.getY(0));
                    vmax = tex.getInterpolatedV(16 * quad.getY(3));
                    break;
                case SOUTH:
                    umin = tex.getInterpolatedU(16 * quad.getX(0));
                    umax = tex.getInterpolatedU(16 * quad.getX(1));
                    vmin = tex.getInterpolatedV(16 * quad.getY(0));
                    vmax = tex.getInterpolatedV(16 * quad.getY(3));
                    break;
                case WEST:
                    umin = tex.getInterpolatedU(16 * (1 - quad.getZ(0)));
                    umax = tex.getInterpolatedU(16 * (1 - quad.getZ(1)));
                    vmin = tex.getInterpolatedV(16 * quad.getY(0));
                    vmax = tex.getInterpolatedV(16 * quad.getY(3));
                    break;
                case EAST:
                    umin = tex.getInterpolatedU(16 * quad.getZ(0));
                    umax = tex.getInterpolatedU(16 * quad.getZ(1));
                    vmin = tex.getInterpolatedV(16 * quad.getY(0));
                    vmax = tex.getInterpolatedV(16 * quad.getY(3));
                    break;
                default:
                    throw new IllegalStateException("Unknown EnumFacing value!");
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
