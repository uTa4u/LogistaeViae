package su.uTa4u.logistaeviae.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.client.render.PipeInstancedRenderer;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OpenGlHelper;initializeTextures()V"
            )
    )
    private void logistaeviea_initRendererInstance(CallbackInfo ci) {
        // Idk why this is required, but thanks to Nali it works
        if (LogistaeViae.IS_INSTANCED_RENDERING) {
            PipeInstancedRenderer.initInstance();
        }
    }
}
