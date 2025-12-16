package su.uTa4u.logistaeviae.client.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.animation.FastTESR;
import su.uTa4u.logistaeviae.tileentity.TileEntitySimplePipe;

import javax.annotation.Nonnull;

// FIXME: fix missing texture, fix missing break particles, remove unused models, fix missing item model
public final class TileEntitySimplePipeRenderer extends FastTESR<TileEntitySimplePipe> {

    @Override
    public void renderTileEntityFast(@Nonnull TileEntitySimplePipe pipe, double x, double y, double z, float partialTicks, int destroyStage, float partial, @Nonnull BufferBuilder buffer) {
        BlockPos pos = pipe.getPos();
        IBlockAccess world = MinecraftForgeClient.getRegionRenderCache(pipe.getWorld(), pos);
        IBlockState state = world.getBlockState(pos);
        TextureAtlasSprite tex = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
        int light = state.getPackedLightmapCoords(world, pos);
        int skyLight = (light >> 16) & 0xFFFF;
        int blockLight = light & 0xFFFF;
        float r = 0.0f;
        float g = 1.0f;
        float b = 0.0f;
        float a = 1.0f;
        buffer.setTranslation(x, y, z);

        // Bottom face (Y = 0)
        buffer.pos(0, 0, 0).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(1, 0, 0).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(1, 0, 1).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(0, 0, 1).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();

        // Top face (Y = 1)
        buffer.pos(0, 1, 0).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(0, 1, 1).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(1, 1, 1).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(1, 1, 0).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();

        // North face (Z = 0)
        buffer.pos(0, 0, 0).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(0, 1, 0).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(1, 1, 0).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(1, 0, 0).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();

        // South face (Z = 1)
        buffer.pos(0, 0, 1).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(1, 0, 1).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(1, 1, 1).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(0, 1, 1).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();

        // West face (X = 0)
        buffer.pos(0, 0, 0).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(0, 0, 1).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(0, 1, 1).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(0, 1, 0).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();

        // East face (X = 1)
        buffer.pos(1, 0, 0).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(1, 1, 0).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(1, 1, 1).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();
        buffer.pos(1, 0, 1).color(r, g, b, a).tex(0, 0).lightmap(skyLight, blockLight).endVertex();

        buffer.setTranslation(0, 0, 0);
    }

}
