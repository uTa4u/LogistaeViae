package su.uTa4u.logistaeviae.mixin;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.BlockModelShapes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.uTa4u.logistaeviae.block.ModBlocks;

@Mixin(value = BlockModelShapes.class)
public abstract class BlockModelShapesMixin {

    @Shadow
    public abstract void registerBuiltInBlocks(Block... builtIns);

    @Inject(
            method = "registerAllBlocks",
            at = @At(
                    value = "HEAD"
            )
    )
    private void logistaeviae_addBuiltInBlocks(CallbackInfo ci) {
        registerBuiltInBlocks(ModBlocks.SIMPLE_PIPE);
    }
}
