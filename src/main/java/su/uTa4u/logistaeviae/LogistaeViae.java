package su.uTa4u.logistaeviae;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import su.uTa4u.logistaeviae.block.BlockPipe;
import su.uTa4u.logistaeviae.block.ModBlocks;
import su.uTa4u.logistaeviae.proxy.IProxy;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION)
public final class LogistaeViae {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    // TODO: put this in a config option
    //       use GLContext.getCapabilities().OpenGL42 to check for required opengl version
    public static final boolean IS_INSTANCED_RENDERING = true;

    @SidedProxy(clientSide = "su.uTa4u.logistaeviae.proxy.ClientProxy", serverSide = "su.uTa4u.logistaeviae.proxy.ServerProxy")
    private static IProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @EventBusSubscriber(value = Side.CLIENT)
    private static final class ClientEventHandler {
        private ClientEventHandler() {
        }

        @SubscribeEvent
        public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
            for (BlockPipe pipe : ModBlocks.PIPES) {
                event.getMap().registerSprite(pipe.getTexture());
            }
        }
    }
}
