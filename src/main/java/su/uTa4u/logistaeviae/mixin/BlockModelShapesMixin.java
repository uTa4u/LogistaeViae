package su.uTa4u.logistaeviae.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import su.uTa4u.logistaeviae.block.BlockPipe;
import su.uTa4u.logistaeviae.block.ModBlocks;

@Mixin(value = BlockModelShapes.class)
public abstract class BlockModelShapesMixin {

    @Shadow
    public abstract void registerBuiltInBlocks(Block... builtIns);

    @Shadow
    private @Final ModelManager modelManager;

    @Inject(
            method = "registerAllBlocks",
            at = @At(
                    value = "HEAD"
            )
    )
    private void logistaeviae_addBuiltInBlocks(CallbackInfo ci) {
        for (Block pipe : ModBlocks.PIPES) {
            registerBuiltInBlocks(pipe);
        }
    }

    @Inject(
            method = "getTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/BlockModelShapes;getModelForState(Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/client/renderer/block/model/IBakedModel;"
            ),
            cancellable = true
    )
    private void logistaeviae_returnParticleTex(CallbackInfoReturnable<TextureAtlasSprite> cir, @Local Block block) {
        if (block instanceof BlockPipe) {
            cir.setReturnValue(modelManager.getTextureMap().getAtlasSprite("minecraft:blocks/glass"));
        }
    }
}