package su.uTa4u.logistaeviae.mixin;

import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(targets = "net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper")
public interface VanillaModelWrapperInvoker {

    // TODO: uncomment after fix by rong is made
//    @Invoker("<init>")
//    @Coerce
//    static Object ctor(
//            ModelLoader loader,
//            ResourceLocation location,
//            ModelBlock model,
//            boolean uvlock,
//            ModelBlockAnimation animation
//    ) {
//        throw new AssertionError();
//    }
}
