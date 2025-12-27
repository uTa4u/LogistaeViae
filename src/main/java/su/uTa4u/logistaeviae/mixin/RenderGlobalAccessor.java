package su.uTa4u.logistaeviae.mixin;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(RenderGlobal.class)
public interface RenderGlobalAccessor {

    @Accessor
    List<RenderGlobal.ContainerLocalRenderInformation> getRenderInfos();

    @Accessor
    TextureManager getRenderEngine();
}
