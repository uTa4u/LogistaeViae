package su.uTa4u.logistaeviae.client.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.animation.FastTESR;
import su.uTa4u.logistaeviae.client.model.PipeModelManager;
import su.uTa4u.logistaeviae.client.model.Quad;
import su.uTa4u.logistaeviae.mixin.PerspectiveMapWrapperAccessor;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import javax.annotation.Nonnull;

// TODO: only render if in distance (like 64 blocks or smth)
// TODO: render routed/unrouted overlay
public final class TileEntityPipeRenderer extends FastTESR<TileEntityPipe> {
    @Override
    public void renderTileEntityFast(@Nonnull TileEntityPipe pipe, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer) {
        BlockPos pos = pipe.getPos();
        IBlockAccess world = MinecraftForgeClient.getRegionRenderCache(pipe.getWorld(), pos);
        IBlockState state = world.getBlockState(pos);
        // TODO: using same light for every face is not right, but idc rn
        int light = state.getPackedLightmapCoords(world, pos);
        int skyLight = (light >> 16) & 0xFFFF;
        int blockLight = light & 0xFFFF;

        for (Quad quad : PipeModelManager.getQuadsForPipe(pipe).values()) {
            putPipeQuad(buffer, quad, x, y, z, skyLight, blockLight);
        }

        /*
        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(new ItemStack(pipe.item));

        if (model instanceof BakedItemModel) {
            // TODO: this loop is not needed. Instead just pass TextureAtlasSprite of this item
            for (BakedQuad quad : model.getQuads(null, null, 0)) {
                putItemQuad2d(buffer, quad, x, y, z, skyLight, blockLight);
            }
        } else if (model instanceof PerspectiveMapWrapper) {
            IBakedModel parent = ((PerspectiveMapWrapperAccessor) model).getParent();
            if (parent instanceof SimpleBakedModel) {
                // TODO: get quads for visible sides only using the dot product between face's normal and player's camera
                for (EnumFacing side : EnumFacing.VALUES) {
                    for (BakedQuad quad : parent.getQuads(null, side, 0)) {
                        putBlockQuad(buffer, quad, x, y, z, skyLight, blockLight);
                    }
                }
            }
        } else {
            // TODO: I suspect that some items/blocks might have their own models or smth
            //       those would need to be rendered by minecraft's mechanisms,
            //       but I think rendering non isGui3d items as flat texture and blocks as a simple cube is good
        }
         */

    }

    private static void putPipeQuad(BufferBuilder buffer, Quad quad, double wx, double wy, double wz, int skyLight, int blockLight) {
        VertexFormat bufferFormat = buffer.getVertexFormat();
        if (bufferFormat != DefaultVertexFormats.BLOCK) {
            throw new IllegalStateException("Expected DefaultVertexFormats.BLOCK for buffer, but got " + bufferFormat);
        }

        for (int i = 0; i < 4; i++) {
            buffer.pos(quad.xs[i] + wx, quad.ys[i] + wy, quad.zs[i] + wz);
            buffer.color(0xFF, 0xFF, 0xFF, 0xFF);
            buffer.tex(quad.us[i], quad.vs[i]);
            buffer.lightmap(skyLight, blockLight);
            buffer.endVertex();
        }
    }

    private static void putItemQuad2d(BufferBuilder buffer, BakedQuad quad, double wx, double wy, double wz, int skyLight, int blockLight) {
        VertexFormat format = quad.getFormat();
        if (format != DefaultVertexFormats.ITEM) {
            throw new IllegalStateException("Expected DefaultVertexFormats.ITEM for quad, but got " + format);
        }

        VertexFormat bufferFormat = buffer.getVertexFormat();
        if (bufferFormat != DefaultVertexFormats.BLOCK) {
            throw new IllegalStateException("Expected DefaultVertexFormats.BLOCK for buffer, but got " + bufferFormat);
        }

        TextureAtlasSprite tex = quad.getSprite();
        int[] vertexData = quad.getVertexData();
        double eyeHeight = Minecraft.getMinecraft().player.eyeHeight;

        double start = 0.25;
        double end = start + 0.5;
        double zStart = 0.5;

        double[] xs = new double[]{start, end, end, start};
        double[] ys = new double[]{start, start, end, end};
        float[] us = new float[]{tex.getMinU(), tex.getMaxU(), tex.getMaxU(), tex.getMinU()};
        float[] vs = new float[]{tex.getMaxV(), tex.getMaxV(), tex.getMinV(), tex.getMinV()};
        for (int i = 0; i < 4; i++) {
            bufferPosLookingAtCamera(buffer, wx, wy, wz, xs[i], ys[i], zStart, eyeHeight);
            bufferColor(buffer, vertexData[3 + i * 7]);
            buffer.tex(us[i], vs[i]);
            buffer.lightmap(skyLight, blockLight);
            buffer.endVertex();
        }

    }

    private static void putBlockQuad(BufferBuilder buffer, BakedQuad quad, double wx, double wy, double wz, int skyLight, int blockLight) {
        VertexFormat format = quad.getFormat();

        if (format != DefaultVertexFormats.ITEM) {
            throw new IllegalStateException("Expected DefaultVertexFormats.ITEM for quad, but got " + format);
        }

        if (buffer.getVertexFormat() != DefaultVertexFormats.BLOCK) {
            throw new IllegalStateException("Expected DefaultVertexFormats.BLOCK for buffer, but got " + format);
        }

        int[] vertexData = quad.getVertexData();
        for (int i = 0; i < 4; i++) {
            buffer.pos(
                    (Float.intBitsToFloat(vertexData[0 + i * 7]) * 0.375 + 0.3125) + wx,
                    (Float.intBitsToFloat(vertexData[1 + i * 7]) * 0.375 + 0.3125) + wy,
                    (Float.intBitsToFloat(vertexData[2 + i * 7]) * 0.375 + 0.3125) + wz
            );
            bufferColor(buffer, vertexData[3 + i * 7]);
            buffer.tex(Float.intBitsToFloat(vertexData[4 + i * 7]), Float.intBitsToFloat(vertexData[5 + i * 7]));
            buffer.lightmap(skyLight, blockLight);
            buffer.endVertex();
        }

    }

    private static void bufferColor(BufferBuilder buffer, int color) {
        buffer.color((color >>> 16) & 0xFF, (color >>> 8) & 0xFF, color & 0xFF, (color >>> 24) & 0xFF);
    }

    // TODO: It is actually looking at the player's head, not at the camera (visible in f5 mode)
    private static void bufferPosLookingAtCamera(BufferBuilder buffer, double wx, double wy, double wz, double lx, double ly, double lz, double eyeHeight) {
        // forward
        double fx = -wx - 0.5;
        double fy = -wy - 0.5 + eyeHeight;
        double fz = -wz - 0.5;
        double fd = MathHelper.sqrt(fx * fx + fy * fy + fz * fz);
        if (fd < 1e-6) {
            buffer.pos(wx + lx, wy + ly, wz + lz);
            return;
        }
        fx /= fd;
        fy /= fd;
        fz /= fd;

        // default up
        double upX = 0.0;
        double upY = 1.0;
        double upZ = 0.0;

        // right
        double rx = upY * fz - upZ * fy;
        double ry = upZ * fx - upX * fz;
        double rz = upX * fy - upY * fx;
        double rd = MathHelper.sqrt(rx * rx + ry * ry + rz * rz);
        if (rd < 1e-6) {
            upX = 1.0;
            upY = 0.0;
            upZ = 0.0;
            rx = upY * fz - upZ * fy;
            ry = upZ * fx - upX * fz;
            rz = upX * fy - upY * fx;
            rd = MathHelper.sqrt(rx * rx + ry * ry + rz * rz);
        }
        if (rd > 1e-6) {
            rx /= rd;
            ry /= rd;
            rz /= rd;
        } else {
            // pls don't happen
            rx = 1.0;
            ry = 0.0;
            rz = 0.0;
        }

        // up
        double ux = fy * rz - fz * ry;
        double uy = fz * rx - fx * rz;
        double uz = fx * ry - fy * rx;
        double ud = MathHelper.sqrt(ux * ux + uy * uy + uz * uz);
        if (ud > 1e-6) {
            ux /= ud;
            uy /= ud;
            uz /= ud;
        } else {
            // pls don't happen
            ux = 0.0;
            uy = 1.0;
            uz = 0.0;
        }

        double clx = lx - 0.5;
        double cly = ly - 0.5;
        double clz = lz - 0.5;

        buffer.pos(
                rx * clx + ux * cly + fx * clz + 0.5 + wx,
                ry * clx + uy * cly + fy * clz + 0.5 + wy,
                rz * clx + uz * cly + fz * clz + 0.5 + wz
        );
    }


}
