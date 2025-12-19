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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.animation.FastTESR;
import su.uTa4u.logistaeviae.mixin.PerspectiveMapWrapperAccessor;
import su.uTa4u.logistaeviae.tileentity.TileEntitySimplePipe;

import javax.annotation.Nonnull;
import java.util.Random;

// TODO: fix missing texture, fix missing break particles, remove unused models, fix missing item model
//        add checks for formats etc
//        steal textures from LP for mc 1.2.5 lol
//        only render if in distance (like 64 blocks or smth)
public final class TileEntitySimplePipeRenderer extends FastTESR<TileEntitySimplePipe> {

    private static final Random RNG = new Random(1);

    @Override
    public void renderTileEntityFast(@Nonnull TileEntitySimplePipe pipe, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer) {
        BlockPos pos = pipe.getPos();
        IBlockAccess world = MinecraftForgeClient.getRegionRenderCache(pipe.getWorld(), pos);
        IBlockState state = world.getBlockState(pos);
        int light = state.getPackedLightmapCoords(world, pos);
        int skyLight = (light >> 16) & 0xFFFF;
        int blockLight = light & 0xFFFF;
        float r = 0.0f;
        float g = 1.0f;
        float b = 0.0f;
        float a = 1.0f;

        Item item = null;
        while (item == null) {
            // TODO: blocks aren't rendered properly because coords are hardcoded in putQuad
            //       blocks should take up the entity inside of the pipe, items should face the player
            int id = RNG.nextInt(128);
            item = Item.getItemById(id);
        }
        item = Items.APPLE;

        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(new ItemStack(item));

        if (model instanceof BakedItemModel) {
            for (BakedQuad quad : model.getQuads(null, null, 0)) {
                this.putQuad(buffer, quad, x, y, z, skyLight, blockLight);
            }
        } else if (model instanceof PerspectiveMapWrapper) {
            IBakedModel parent = ((PerspectiveMapWrapperAccessor) model).getParent();
            if (parent instanceof SimpleBakedModel) {
                // TODO: get quads for visible sides only
                for (EnumFacing side : EnumFacing.VALUES) {
                    for (BakedQuad quad : parent.getQuads(null, side, 0)) {
                        this.putQuad(buffer, quad, x, y, z, skyLight, blockLight);
                    }
                }
            }
        } else {
            // TODO: render missingno
        }

    }

    // quad is in DefaultVertexFormats.ITEM format
    private void putQuad(@Nonnull BufferBuilder buffer, BakedQuad quad, double x, double y, double z, int skyLight, int blockLight) {
        VertexFormat format = quad.getFormat();

        if (format != DefaultVertexFormats.ITEM) {
            throw new IllegalStateException("Expected DefaultVertexFormats.ITEM for quad, but got " + format);
        }

        if (buffer.getVertexFormat() != DefaultVertexFormats.BLOCK) {
            throw new IllegalStateException("Expected DefaultVertexFormats.BLOCK for buffer, but got " + format);
        }

        TextureAtlasSprite tex = quad.getSprite();
        int[] vertexData = quad.getVertexData();

        double eyeHeight = Minecraft.getMinecraft().player.eyeHeight;

        double start = 0.25;
        double end = start + 0.5;
        double zStart = 0.5;

        int offset = 0;
        bufferPos(buffer, x, y, z, start, start, zStart, eyeHeight);
        bufferColor(buffer, vertexData[3 + offset]);
        buffer.tex(tex.getMinU(), tex.getMaxV());
        buffer.lightmap(skyLight, blockLight);
        buffer.endVertex();

        offset += 7;
        bufferPos(buffer, x, y, z, end, start, zStart, eyeHeight);
        bufferColor(buffer, vertexData[3 + offset]);
        buffer.tex(tex.getMaxU(), tex.getMaxV());
        buffer.lightmap(skyLight, blockLight);
        buffer.endVertex();

        offset += 7;
        bufferPos(buffer, x, y, z, end, end, zStart, eyeHeight);
        bufferColor(buffer, vertexData[3 + offset]);
        buffer.tex(tex.getMaxU(), tex.getMinV());
        buffer.lightmap(skyLight, blockLight);
        buffer.endVertex();

        offset += 7;
        bufferPos(buffer, x, y, z, start, end, zStart, eyeHeight);
        bufferColor(buffer, vertexData[3 + offset]);
        buffer.tex(tex.getMinU(), tex.getMinV());
        buffer.lightmap(skyLight, blockLight);
        buffer.endVertex();

    }

    private static void bufferColor(BufferBuilder buffer, int color) {
        buffer.color((color >>> 16) & 0xFF, (color >>> 8) & 0xFF, color & 0xFF, (color >>> 24) & 0xFF);
    }

    private static void bufferPos(BufferBuilder buffer, double wx, double wy, double wz, double lx, double ly, double lz, double eyeHeight) {
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
