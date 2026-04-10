package su.uTa4u.logistaeviae.logic;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

// FIXME: implement incremental network writing/reading
// TODO: add all missing items/blocks from LP
// TODO: start implementing A* algorithm
public final class PipeNetwork {

    // For testing purposes
    private static int networkID = 0;
    private static final Item[] TEST_ITEMS = new Item[]{Items.APPLE, Items.BREAD, Item.getItemFromBlock(Blocks.SAND), Item.getItemFromBlock(Blocks.STONE)};
    public Item item;

    private final Set<TileEntityPipe> pipes = new HashSet<>();
    private boolean isDirty = false;

    PipeNetwork() {
        this.item = TEST_ITEMS[networkID++];
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public void markDirty() {
        this.isDirty = true;
    }

    public PipeRoute getRoute(BlockPos pos1, BlockPos pos2) {
        return null;
    }

    void deserializeNBT() {

    }

    @Nonnull
    NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();

        this.isDirty = false;
        return nbt;
    }

    // Taken from ChunkPos#asLong
    private static long getChunkKey(BlockPos pos) {
        return (long)(pos.getX() >> 4) & 4294967295L | ((long)(pos.getZ() >> 4) & 4294967295L) << 32;
    }
}
