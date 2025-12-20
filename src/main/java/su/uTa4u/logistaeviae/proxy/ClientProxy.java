package su.uTa4u.logistaeviae.proxy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import su.uTa4u.logistaeviae.block.ModBlocks;
import su.uTa4u.logistaeviae.client.render.TileEntitySimplePipeRenderer;
import su.uTa4u.logistaeviae.tileentity.TileEntitySimplePipe;

import java.util.Objects;

public class ClientProxy implements IProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerTileEntity(TileEntitySimplePipe.class, Objects.requireNonNull(ModBlocks.SIMPLE_PIPE.getRegistryName()));
    }

    @Override
    public void init(FMLInitializationEvent event) {
        ModBlocks.registerRender();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySimplePipe.class, new TileEntitySimplePipeRenderer());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
    }
}
