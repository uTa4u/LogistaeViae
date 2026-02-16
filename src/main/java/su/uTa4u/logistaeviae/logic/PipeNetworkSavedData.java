package su.uTa4u.logistaeviae.logic;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import su.uTa4u.logistaeviae.Tags;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PipeNetworkSavedData extends WorldSavedData {
    private static final String TAG_DIMENSION = "Dimension";
    private static final String TAG_SAVED_DATA = "PipeNetworkSavedData";
    private static final String NAME = Tags.MOD_ID + "_" + TAG_SAVED_DATA;

    // TODO: maybe try using UUID as key
    // TODO: most chunks have a 1-2 networks so using a Set is an overkill
    // int dimension and long chunkpos keys
    private final Int2ObjectMap<Long2ObjectMap<Set<PipeNetwork>>> networksByDimByChunkPos = new Int2ObjectArrayMap<>();
    private final Int2ObjectMap<Set<PipeNetwork>> networksByDim = new Int2ObjectArrayMap<>();

    private PipeNetworkSavedData() {
        super(NAME);
    }

    void putNetwork(int dimID, long chunkKey, PipeNetwork pipeNetwork) {
        this.networksByDim.computeIfAbsent(dimID, i -> new HashSet<>()).add(pipeNetwork);
        this.networksByDimByChunkPos
                .computeIfAbsent(dimID, i -> new Long2ObjectOpenHashMap<>())
                .computeIfAbsent(chunkKey, i -> new HashSet<>())
                .add(pipeNetwork);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {

    }

    // FIXME: implement incremental network writing/reading
    // TODO: add all missing items/blocks from LP
    // TODO: start implementing A* algorithm
    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        NBTTagList dataTag = new NBTTagList();
        for (Int2ObjectMap.Entry<Long2ObjectMap<Set<PipeNetwork>>> dimEntry : this.networksByDimByChunkPos.int2ObjectEntrySet()) {
            NBTTagCompound dimTag = new NBTTagCompound();
            dimTag.setInteger(TAG_DIMENSION, dimEntry.getIntKey());

            dataTag.appendTag(dimTag);
        }
        nbt.setTag(TAG_SAVED_DATA, dataTag);

        return nbt;
    }

    public static PipeNetworkSavedData get(World world) {
        MapStorage storage = Objects.requireNonNull(world.getMapStorage());
        PipeNetworkSavedData instance = (PipeNetworkSavedData) storage.getOrLoadData(PipeNetworkSavedData.class, NAME);
        if (instance == null) {
            instance = new PipeNetworkSavedData();
            storage.setData(NAME, instance);
        }
        return instance;
    }
}
