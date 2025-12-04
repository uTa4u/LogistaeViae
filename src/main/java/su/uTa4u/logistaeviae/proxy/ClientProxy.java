package su.uTa4u.logistaeviae.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import su.uTa4u.logistaeviae.blocks.ModBlocks;

public class ClientProxy implements IProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {

    }

    @Override
    public void init(FMLInitializationEvent event) {
        ModBlocks.registerRender();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }
}
