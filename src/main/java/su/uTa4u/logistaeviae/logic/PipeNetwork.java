package su.uTa4u.logistaeviae.logic;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

// FIXME: implement incremental network writing/reading
// TODO: add all missing items/blocks from LP
// TODO: start implementing A* algorithm
public final class PipeNetwork {
    private static final String TAG_ID = "Id";
    private static final String TAG_DIMENSION = "Dimension";
    private static final String TAG_POSITIONS = "Positions";
    private static final String TAG_PIPES = "Pipes";

    private final Int2ObjectMap<Set<BlockPos>> pipes = new Int2ObjectOpenHashMap<>();
    private final PipeNetworkSavedData savedData;
    private int networkID;

    PipeNetwork(int networkID, PipeNetworkSavedData savedData) {
        this.networkID = networkID;
        this.savedData = savedData;
    }

    public void add(int dim, BlockPos pos) {
        this.pipes.computeIfAbsent(dim, (i) -> new HashSet<>()).add(pos);
        this.savedData.putIdByPos(pos, this.networkID);
        this.savedData.markDirty();
    }

    public void remove(int dim, BlockPos pos) {
        this.pipes.computeIfAbsent(dim, (i) -> new HashSet<>()).remove(pos);
        this.savedData.removeIdByPos(pos);
        this.savedData.markDirty();
    }

    public void merge(PipeNetwork that) {
        if (this == that) return;
        for (Int2ObjectMap.Entry<Set<BlockPos>> entry : that.pipes.int2ObjectEntrySet()) {
            this.pipes.getOrDefault(entry.getIntKey(), new HashSet<>()).addAll(entry.getValue());
        }
        this.savedData.removeNetwork(that.networkID);
        this.savedData.markDirty();
    }

    public void forEach(Consumer<BlockPos> consumer) {
        for (Int2ObjectMap.Entry<Set<BlockPos>> entry : this.pipes.int2ObjectEntrySet()) {
            entry.getValue().forEach(consumer);
        }
    }

    public PipeRoute getRoute(BlockPos pos1, BlockPos pos2) {
        return null;
    }

    void readFromNBT(NBTTagCompound nbt) {
        this.pipes.clear();
        NBTTagList pipes = nbt.getTagList(TAG_PIPES, 10);
        for (int i = 0; i < pipes.tagCount(); i++) {
            NBTTagCompound dim = pipes.getCompoundTagAt(i);
            NBTTagList positions = dim.getTagList(TAG_POSITIONS, 10);
            Set<BlockPos> set = new HashSet<>();
            for (int j = 0; j < positions.tagCount(); j++) {
                set.add(NBTUtil.getPosFromTag(positions.getCompoundTagAt(j)));
            }
            this.pipes.put(dim.getInteger(TAG_DIMENSION), set);
        }

        this.networkID = nbt.getInteger(TAG_ID);
        if (this.networkID == 0) this.networkID = 1;
    }

    @Nonnull
    NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList pipes = new NBTTagList();
        for (Int2ObjectMap.Entry<Set<BlockPos>> entry : this.pipes.int2ObjectEntrySet()) {
            NBTTagCompound dim = new NBTTagCompound();
            dim.setInteger(TAG_DIMENSION, entry.getIntKey());
            NBTTagList positions = new NBTTagList();
            for (BlockPos pos : entry.getValue()) {
                positions.appendTag(NBTUtil.createPosTag(pos));
            }
            dim.setTag(TAG_POSITIONS, positions);
            pipes.appendTag(dim);
        }
        nbt.setTag(TAG_PIPES, pipes);

        nbt.setInteger(TAG_ID, this.networkID);

        return nbt;
    }
}
