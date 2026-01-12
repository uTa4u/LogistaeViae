package su.uTa4u.logistaeviae.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.Tags;
import su.uTa4u.logistaeviae.block.BlockPipe;
import su.uTa4u.logistaeviae.block.ModBlocks;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Mixin(ModelLoader.class)
public abstract class ModelLoaderMixin {

    @Unique
    private static Constructor<?> VANILLA_MODEL_WRAPPER_CTOR = null;

    @Unique
    private static final ModelBlockAnimation DEFAULT_BLOCK_ANIMATION = new ModelBlockAnimation(ImmutableMap.of(), ImmutableMap.of());

    @Shadow
    private @Final Map<ModelResourceLocation, IModel> stateModels;

    @Inject(
            method = "loadItemModels",
            at = @At(
                    value = "TAIL"
            )
    )
    private void logistaeviae_loadPipeModel(CallbackInfo ci) {
        if (VANILLA_MODEL_WRAPPER_CTOR == null) {
            try {
                VANILLA_MODEL_WRAPPER_CTOR = Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaModelWrapper").getDeclaredConstructor(ModelLoader.class, ResourceLocation.class, ModelBlock.class, boolean.class, ModelBlockAnimation.class);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                throw new RuntimeException("Couldn't access constructor of ModelLoader$VanillaModelWrapper!", e);
            }
        }

        IModel model;
        try {
            model = ModelLoaderRegistry.getModel(new ResourceLocation(Tags.MOD_ID, "item/pipe"));
        } catch (Exception e) {
            LogistaeViae.LOGGER.error("Could not load pipe item model!");
            return;
        }

        Optional<ModelBlock> modelBlockOpt = model.asVanillaModel();
        if (!modelBlockOpt.isPresent()) {
            LogistaeViae.LOGGER.error("Loaded item pipe model doesn't have a vanilla model!");
            return;
        }

        ModelBlock basePipeModel = modelBlockOpt.get();
        ModelLoader modelLoader = (ModelLoader) (Object) this;

        for (BlockPipe blockPipe : ModBlocks.PIPES) {
            ResourceLocation regName = Objects.requireNonNull(blockPipe.getRegistryName());
            ResourceLocation modelLoc = new ResourceLocation(Tags.MOD_ID, "models/item/" + regName.getPath());
            Map<String, String> textures = new HashMap<>();
            textures.put("all", blockPipe.getTexture().toString());
            ModelBlock texturedPipeModel = new ModelBlock(
                    basePipeModel.getParentLocation(),
                    basePipeModel.getElements(),
                    textures,
                    basePipeModel.isAmbientOcclusion(),
                    basePipeModel.isGui3d(),
                    basePipeModel.getAllTransforms(),
                    basePipeModel.getOverrides()
            );
            texturedPipeModel.name = modelLoc.toString();
            texturedPipeModel.parent = basePipeModel.parent;
            try {
                this.stateModels.put(
                        new ModelResourceLocation(regName, "inventory"),
                        (IModel) VANILLA_MODEL_WRAPPER_CTOR.newInstance(modelLoader, modelLoc, texturedPipeModel, false, DEFAULT_BLOCK_ANIMATION)
                );
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Couldn't generate textured model for pipe block: " + regName, e);
            }
        }
    }
}
