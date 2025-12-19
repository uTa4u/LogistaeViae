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
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.animation.FastTESR;
import su.uTa4u.logistaeviae.mixin.PerspectiveMapWrapperAccessor;
import su.uTa4u.logistaeviae.tileentity.TileEntitySimplePipe;

import javax.annotation.Nonnull;
import java.util.Random;

// FIXME: fix missing texture, fix missing break particles, remove unused models, fix missing item model
//        add checks for formats etc
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

        buffer.setTranslation(x, y, z);
        if (model instanceof BakedItemModel) {
            for (BakedQuad quad : model.getQuads(null, null, 0)) {
                this.putQuad(buffer, quad, skyLight, blockLight);
            }
        } else if (model instanceof PerspectiveMapWrapper) {
            IBakedModel parent = ((PerspectiveMapWrapperAccessor) model).getParent();
            if (parent instanceof SimpleBakedModel) {
                // TODO: get quads for visible sides only
                for (EnumFacing side : EnumFacing.VALUES) {
                    for (BakedQuad quad : parent.getQuads(null, side, 0)) {
                        this.putQuad(buffer, quad, skyLight, blockLight);
                    }
                }
            }
        } else {
            // TODO: render missingno
        }
        buffer.setTranslation(0, 0, 0);

    }

    // quad is in DefaultVertexFormats.ITEM format
    private void putQuad(@Nonnull BufferBuilder buffer, BakedQuad quad, int skyLight, int blockLight) {
        VertexFormat format = quad.getFormat();

        if (format != DefaultVertexFormats.ITEM) {
            throw new IllegalStateException("Expected DefaultVertexFormats.ITEM for quad, but got " + format);
        }

        if (buffer.getVertexFormat() != DefaultVertexFormats.BLOCK) {
            throw new IllegalStateException("Expected DefaultVertexFormats.BLOCK for buffer, but got " + format);
        }

        TextureAtlasSprite tex = quad.getSprite();
        int[] vertexData = quad.getVertexData();

        final double size = 0.375;
        final double start = 0.3125;
        final double zStart = 0.5;

        int offset = 0;
        buffer.pos(start, start, zStart);
        bufferColor(buffer, vertexData[3 + offset]);
        buffer.tex(tex.getMinU(), tex.getMaxV());
        buffer.lightmap(skyLight, blockLight);
        buffer.endVertex();

        offset += 7;
        buffer.pos(start + size, start, zStart);
        bufferColor(buffer, vertexData[3 + offset]);
        buffer.tex(tex.getMaxU(), tex.getMaxV());
        buffer.lightmap(skyLight, blockLight);
        buffer.endVertex();

        offset += 7;
        buffer.pos(start + size, start + size, zStart);
        bufferColor(buffer, vertexData[3 + offset]);
        buffer.tex(tex.getMaxU(), tex.getMinV());
        buffer.lightmap(skyLight, blockLight);
        buffer.endVertex();

        offset += 7;
        buffer.pos(start, start + size, zStart);
        bufferColor(buffer, vertexData[3 + offset]);
        buffer.tex(tex.getMinU(), tex.getMinV());
        buffer.lightmap(skyLight, blockLight);
        buffer.endVertex();

    }

    private static void bufferColor(BufferBuilder buffer, int color) {
        buffer.color((color >>> 16) & 0xFF, (color >>> 8) & 0xFF, color & 0xFF, (color >>> 24) & 0xFF);
    }

}
