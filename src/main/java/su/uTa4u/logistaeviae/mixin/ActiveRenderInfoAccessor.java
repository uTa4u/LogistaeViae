package su.uTa4u.logistaeviae.mixin;

import net.minecraft.client.renderer.ActiveRenderInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.FloatBuffer;

@Mixin(ActiveRenderInfo.class)
public interface ActiveRenderInfoAccessor {

    @Accessor("MODELVIEW")
    static FloatBuffer getViewMatrix() {
        throw new AssertionError();
    }

    @Accessor("PROJECTION")
    static FloatBuffer getProjMatrix() {
        throw new AssertionError();
    }
}
