package su.uTa4u.logistaeviae.logic;

import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber
public final class PipeNetworkHandler {
    private PipeNetworkHandler() {
    }

    @SubscribeEvent
    public static void onWorldTickEvent(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (event.side != Side.SERVER) return;

        PipeNetworkSavedData.get(event.world).forEachNetwork((network) -> {

        });
    }
}
