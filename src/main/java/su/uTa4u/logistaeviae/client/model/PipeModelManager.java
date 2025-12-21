package su.uTa4u.logistaeviae.client.model;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectArrayMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class PipeModelManager {
    private static final float FROM = 0.25f;
    private static final float TOOO = 0.75f;

    // ArrayMap implementation should be fine for only 64 entries
    private static final Map<TextureAtlasSprite, Byte2ObjectMap<EnumMap<EnumFacing, Quad>>> CACHE = new HashMap<>();

    public static EnumMap<EnumFacing, Quad> getQuadsForPipe(TileEntityPipe pipe) {
        TextureAtlasSprite tex = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(pipe.getBlockPipe().getTexture().toString());
        if (!CACHE.containsKey(tex)) {
            CACHE.put(tex, new Byte2ObjectArrayMap<>());
        }
        Byte2ObjectMap<EnumMap<EnumFacing, Quad>> map = CACHE.get(tex);
        byte packedConnections = pipe.packConnections();
        if (!map.containsKey(packedConnections)) {
            map.put(packedConnections, computeQuadsForPipe(pipe, tex));
        }
        return map.get(packedConnections);
    }

    private static EnumMap<EnumFacing, Quad> computeQuadsForPipe(TileEntityPipe pipe, TextureAtlasSprite tex) {
        float umin = tex.getMinU();
        float umax = tex.getMaxU();
        float vmin = tex.getMinV();
        float vmax = tex.getMaxV();

        EnumMap<EnumFacing, Quad> center = getCenter(tex);
        pipe.forEachConnection((connection) -> {
            switch (connection) {
                case DOWN: {
                    // X and Z axis quads extend down
                    center.get(EnumFacing.NORTH).extendDown(vmin);
                    center.get(EnumFacing.SOUTH).extendDown(vmax);
                    center.get(EnumFacing.WEST).extendDown(vmax);
                    center.get(EnumFacing.EAST).extendDown(vmin);
                    break;
                }
                case UP: {
                    // X and Z axis quads extend
                    center.get(EnumFacing.NORTH).extendUp(vmax);
                    center.get(EnumFacing.SOUTH).extendUp(vmin);
                    center.get(EnumFacing.WEST).extendUp(vmin);
                    center.get(EnumFacing.EAST).extendUp(vmax);
                    break;
                }
                case NORTH: {
                    // Y and X axis quads extend north
                    center.get(EnumFacing.DOWN).extendNorth(umin);
                    center.get(EnumFacing.UP).extendNorth(umin);
                    center.get(EnumFacing.WEST).extendNorth(umin);
                    center.get(EnumFacing.EAST).extendNorth(umin);
                    break;
                }
                case SOUTH: {
                    // Y and X axis quads extend south
                    center.get(EnumFacing.DOWN).extendSouth(umax);
                    center.get(EnumFacing.UP).extendSouth(umax);
                    center.get(EnumFacing.WEST).extendSouth(umax);
                    center.get(EnumFacing.EAST).extendSouth(umax);
                    break;
                }
                case WEST: {
                    // Y and Z axis quads extend west
                    // This is an edge case because my original idea failed :sob:
                    Quad qd = center.get(EnumFacing.DOWN);
                    qd.xs[2] = 0.0f;
                    qd.xs[3] = 0.0f;
                    qd.vs[2] = vmax;
                    qd.vs[3] = vmax;
                    Quad qu = center.get(EnumFacing.UP);
                    qu.xs[2] = 0.0f;
                    qu.xs[3] = 0.0f;
                    qu.vs[2] = vmin;
                    qu.vs[3] = vmin;
                    center.get(EnumFacing.NORTH).extendWest(umax);
                    center.get(EnumFacing.SOUTH).extendWest(umax);
                    break;
                }
                case EAST: {
                    // Y and Z axis quads extend east
                    // This is an edge case because my original idea failed :sob:
                    Quad qd = center.get(EnumFacing.DOWN);
                    qd.xs[0] = 1.0f;
                    qd.xs[1] = 1.0f;
                    qd.vs[0] = vmin;
                    qd.vs[1] = vmin;
                    Quad qu = center.get(EnumFacing.UP);
                    qu.xs[0] = 1.0f;
                    qu.xs[1] = 1.0f;
                    qu.vs[0] = vmax;
                    qu.vs[1] = vmax;
                    center.get(EnumFacing.NORTH).extendEast(umin);
                    center.get(EnumFacing.SOUTH).extendEast(umin);
                    break;
                }
            }
        });
        return center;
    }

    private static EnumMap<EnumFacing, Quad> getCenter(TextureAtlasSprite tex) {
        float umin = tex.getInterpolatedU(4);
        float umax = tex.getInterpolatedU(12);
        float vmin = tex.getInterpolatedV(4);
        float vmax = tex.getInterpolatedV(12);

        EnumMap<EnumFacing, Quad> quads = new EnumMap<>(EnumFacing.class);
        quads.put(EnumFacing.DOWN,
                new Quad(
                        TOOO, FROM, FROM, umin, vmin,
                        TOOO, FROM, TOOO, umax, vmin,
                        FROM, FROM, TOOO, umax, vmax,
                        FROM, FROM, FROM, umin, vmax
                )
        );
        quads.put(EnumFacing.UP,
                new Quad(
                        TOOO, TOOO, FROM, umin, vmax,
                        TOOO, TOOO, TOOO, umax, vmax,
                        FROM, TOOO, TOOO, umax, vmin,
                        FROM, TOOO, FROM, umin, vmin
                )
        );
        quads.put(EnumFacing.NORTH,
                new Quad(
                        TOOO, FROM, FROM, umin, vmin,
                        FROM, FROM, FROM, umax, vmin,
                        FROM, TOOO, FROM, umax, vmax,
                        TOOO, TOOO, FROM, umin, vmax
                )
        );
        quads.put(EnumFacing.SOUTH,
                new Quad(
                        TOOO, FROM, TOOO, umin, vmax,
                        FROM, FROM, TOOO, umax, vmax,
                        FROM, TOOO, TOOO, umax, vmin,
                        TOOO, TOOO, TOOO, umin, vmin
                )
        );
        quads.put(EnumFacing.WEST,
                new Quad(
                        FROM, FROM, FROM, umin, vmax,
                        FROM, FROM, TOOO, umax, vmax,
                        FROM, TOOO, TOOO, umax, vmin,
                        FROM, TOOO, FROM, umin, vmin
                )
        );
        quads.put(EnumFacing.EAST,
                new Quad(
                        TOOO, FROM, FROM, umin, vmin,
                        TOOO, FROM, TOOO, umax, vmin,
                        TOOO, TOOO, TOOO, umax, vmax,
                        TOOO, TOOO, FROM, umin, vmax
                )
        );
        return quads;
    }

    private PipeModelManager() {
    }
}
