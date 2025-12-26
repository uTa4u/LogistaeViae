package su.uTa4u.logistaeviae.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import su.uTa4u.logistaeviae.LogistaeViae;
import su.uTa4u.logistaeviae.block.ModBlocks;
import su.uTa4u.logistaeviae.client.render.PipeInstancedRenderer;
import su.uTa4u.logistaeviae.client.render.TileEntityPipeRenderer;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

public class ClientProxy implements IProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
    }

    @Override
    public void init(FMLInitializationEvent event) {
        ModBlocks.registerRender();
        if (LogistaeViae.IS_INSTANCED_RENDERING) {
            MinecraftForge.EVENT_BUS.register(PipeInstancedRenderer.instance);
        } else {
            ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPipe.class, new TileEntityPipeRenderer());
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
    }
}
