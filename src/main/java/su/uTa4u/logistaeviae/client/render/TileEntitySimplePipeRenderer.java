package su.uTa4u.logistaeviae.client.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
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
    private void putQuad(@Nonnull final BufferBuilder buffer, BakedQuad quad, int skyLight, int blockLight) {
        VertexFormat format = quad.getFormat();

        if (format != DefaultVertexFormats.ITEM) {
            throw new IllegalStateException("Expected DefaultVertexFormats.ITEM for quad, but got " + format);
        }

        if (buffer.getVertexFormat() != DefaultVertexFormats.BLOCK) {
            throw new IllegalStateException("Expected DefaultVertexFormats.BLOCK for buffer, but got " + format);
        }

        int[] vertexData = quad.getVertexData();
        final double size = 1;
        final double start = 0.0;
        final double zStart = 1;

        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    buffer.pos(start, start, zStart);
                    break;
                case 1:
                    buffer.pos(start + size, start, zStart);
                    break;
                case 2:
                    buffer.pos(start + size, start + size, zStart);
                    break;
                case 3:
                    buffer.pos(start, start + size, zStart);
                    break;
            }

            // 0, 1, 2 is position
            int color = vertexData[3 + i * 7];
            buffer.color(
                    (color >>> 16) & 0xFF,
                    (color >>> 8) & 0xFF,
                    color & 0xFF,
                    (color >>> 24) & 0xFF
            );

            buffer.tex(Float.intBitsToFloat(vertexData[4 + i * 7]), Float.intBitsToFloat(vertexData[5 + i * 7]));

            buffer.lightmap(skyLight, blockLight);

            buffer.endVertex();
        }

    }

}
