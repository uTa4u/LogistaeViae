package su.uTa4u.logistaeviae.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor {

    @Accessor
    float getFarPlaneDistance();

    @Invoker
    float callGetFOVModifier(float partialTicks, boolean useFOVSetting);
}
