package su.uTa4u.logistaeviae.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

@Mixin(RenderChunk.class)
public abstract class RenderChunkMixin {

    @Inject(
            method = "rebuildChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;getRenderer(Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;"
            )
    )
    private void logistaeviea_rebuildChunk(float x, float y, float z, ChunkCompileTaskGenerator generator, CallbackInfo ci, @Local CompiledChunk compiledchunk, @Local TileEntity tileentity) {
        if (LogistaeViae.IS_INSTANCED_RENDERING && tileentity instanceof TileEntityPipe) {
            compiledchunk.addTileEntity(tileentity);
        }
    }
}
