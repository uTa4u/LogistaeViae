package su.uTa4u.logistaeviae.client.model;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;
import su.uTa4u.logistaeviae.block.BlockPipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PipeBakedModel implements IBakedModel {
    private final TextureAtlasSprite particle = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/glass");

    @Override
    @Nonnull
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        IBakedModel fallback = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
        if (state == null) return fallback.getQuads(null, side, rand);
        Block block = state.getBlock();
        ResourceLocation texLoc = block instanceof BlockPipe ? ((BlockPipe) block).getTexture() : TextureMap.LOCATION_MISSING_TEXTURE;
        return PipeModelManager.getTexturedBakedModelForPipe(texLoc, ((IExtendedBlockState) state).getValue(BlockPipe.CONNECTION_PROP));
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    @Nonnull
    public TextureAtlasSprite getParticleTexture() {
        return this.particle;
    }

    @Override
    @Nonnull
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}