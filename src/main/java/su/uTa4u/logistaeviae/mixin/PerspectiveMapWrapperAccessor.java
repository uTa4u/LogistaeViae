package su.uTa4u.logistaeviae.mixin;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PerspectiveMapWrapper.class)
public interface PerspectiveMapWrapperAccessor {

    @Accessor
    IBakedModel getParent();
}
