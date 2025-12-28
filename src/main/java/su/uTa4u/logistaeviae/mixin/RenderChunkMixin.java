package su.uTa4u.logistaeviae.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.client.render.PipeInstancedRenderer;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

@Mixin(RenderChunk.class)
public abstract class RenderChunkMixin {

    @Shadow
    private @Final BlockPos.MutableBlockPos position;

    @Inject(
            method = "rebuildChunk",
            at = @At(
                    value = "TAIL"
            )
    )
    private void logistaeviae_setChuckRebuilt(float x, float y, float z, ChunkCompileTaskGenerator generator, CallbackInfo ci) {
        if (LogistaeViae.IS_INSTANCED_RENDERING) {
            PipeInstancedRenderer.instance.setChuckRebuilt(this.position);
        }
    }

    @Inject(
            method = "rebuildChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;getRenderer(Lnet/minecraft/tileentity/TileEntity;)Lnet/minecraft/client/renderer/tileentity/TileEntitySpecialRenderer;"
            )
    )
    private void logistaeviae_addPipe(float x, float y, float z, ChunkCompileTaskGenerator generator, CallbackInfo ci, @Local TileEntity tileentity) {
        if (LogistaeViae.IS_INSTANCED_RENDERING && tileentity instanceof TileEntityPipe) {
            PipeInstancedRenderer.instance.addPipe(this.position, (TileEntityPipe) tileentity);
        }
    }
}
