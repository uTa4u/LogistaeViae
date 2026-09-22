package su.uTa4u.logistaeviae.logic;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
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
        PipeWorldSavedData.get(event.world).tickAll(event.world);
    }

    @SubscribeEvent
    public static void onChunkLoadEvent(ChunkEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        Chunk chunk = event.getChunk();
        int cx = chunk.x;
        int cz = chunk.z;
        PipeWorldSavedData data = PipeWorldSavedData.get(world);
        for (int cy = 0; cy < 16; cy++) {
            data.onChunkLoad(cx, cy, cz);
        }
    }

    @SubscribeEvent
    public static void onChunkUnloadEvent(ChunkEvent.Unload event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        Chunk chunk = event.getChunk();
        int cx = chunk.x;
        int cz = chunk.z;
        PipeWorldSavedData data = PipeWorldSavedData.get(world);
        for (int cy = 0; cy < 16; cy++) {
            data.onChunkUnload(cx, cy, cz);
        }
    }

}
