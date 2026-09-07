package su.uTa4u.logistaeviae.model;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: maybe remove this class and move everything into PipeBakedModel
public final class PipeModelManager {
    public static final int BASE_INSTANCE_COUNT = 64;

    private static final float FROM = 0.25f;
    private static final float TOOO = 0.75f;

    private static final Map<TextureAtlasSprite, Byte2ObjectMap<List<BakedQuad>>> TEXTURED_BAKEDMODEL_CACHE = new HashMap<>();

    public static List<BakedQuad> getTexturedBakedModelForPipe(ResourceLocation texLoc, byte packedConnections) {
        TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(texLoc.toString());

        Byte2ObjectMap<List<BakedQuad>> modelMap = TEXTURED_BAKEDMODEL_CACHE.computeIfAbsent(tex, i -> new Byte2ObjectArrayMap<>());

        List<BakedQuad> model = modelMap.get(packedConnections);
        if (model == null) {
            EnumMap<EnumFacing, PipeQuad> rawModel = computeQuadsForPipe(packedConnections);
            texture(rawModel, tex);
            ImmutableList.Builder<BakedQuad> builder = new ImmutableList.Builder<>();
            for (Map.Entry<EnumFacing, PipeQuad> entry : rawModel.entrySet()) {
                builder.add(entry.getValue().bake(entry.getKey()));
            }
            model = builder.build();
            modelMap.put(packedConnections, model);
        }
        return model;
    }

    private static EnumMap<EnumFacing, PipeQuad> computeQuadsForPipe(byte packedConnections) {
        EnumMap<EnumFacing, PipeQuad> model = getCenter();

        for (EnumFacing connection : TileEntityPipe.unpackConnections(packedConnections)) {
            PipeQuad quad;
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
        }
        return model;
    }

    private static EnumMap<EnumFacing, PipeQuad> getCenter() {
        EnumMap<EnumFacing, PipeQuad> quads = new EnumMap<>(EnumFacing.class);
        quads.put(EnumFacing.DOWN,
                new PipeQuad(
                        FROM, FROM, FROM,
                        TOOO, FROM, FROM,
                        TOOO, FROM, TOOO,
                        FROM, FROM, TOOO
                )
        );
        quads.put(EnumFacing.UP,
                new PipeQuad(
                        TOOO, TOOO, FROM,
                        FROM, TOOO, FROM,
                        FROM, TOOO, TOOO,
                        TOOO, TOOO, TOOO
                )
        );
        quads.put(EnumFacing.NORTH,
                new PipeQuad(
                        TOOO, FROM, FROM,
                        FROM, FROM, FROM,
                        FROM, TOOO, FROM,
                        TOOO, TOOO, FROM
                )
        );
        quads.put(EnumFacing.SOUTH,
                new PipeQuad(
                        FROM, FROM, TOOO,
                        TOOO, FROM, TOOO,
                        TOOO, TOOO, TOOO,
                        FROM, TOOO, TOOO
                )
        );
        quads.put(EnumFacing.WEST,
                new PipeQuad(
                        FROM, FROM, FROM,
                        FROM, FROM, TOOO,
                        FROM, TOOO, TOOO,
                        FROM, TOOO, FROM
                )
        );
        quads.put(EnumFacing.EAST,
                new PipeQuad(
                        TOOO, FROM, TOOO,
                        TOOO, FROM, FROM,
                        TOOO, TOOO, FROM,
                        TOOO, TOOO, TOOO
                )
        );
        return quads;
    }

    // These are not technically correct UV coords, some are flipped
    private static void texture(EnumMap<EnumFacing, PipeQuad> model, TextureAtlasSprite tex) {
        float umin;
        float umax;
        float vmin;
        float vmax;
        for (EnumFacing dir : EnumFacing.VALUES) {
            PipeQuad quad = model.get(dir);
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
                default:
                    throw new AssertionError("Unknown EnumFacing value!");
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
