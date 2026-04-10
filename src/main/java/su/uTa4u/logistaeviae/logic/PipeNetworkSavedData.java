package su.uTa4u.logistaeviae.logic;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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

public final class PipeNetworkSavedData extends WorldSavedData {
    private static final String TAG_ID = "Id";
    private static final String TAG_NETWORK = "Network";
    private static final String TAG_NETWORK_LIST = "Networks";
    private static final String TAG_NEXT_ID = "NextId";
    private static final String NAME = Tags.MOD_ID + "_PipeNetworkSavedData";

    // TODO: maybe try using UUID as key
    private final Int2ObjectMap<PipeNetwork> networks = new Int2ObjectOpenHashMap<>();
    private int networkID = 0;

    private PipeNetworkSavedData() {
        super(NAME);
    }

    public PipeNetwork getNetwork(int id) {
        return this.networks.get(id);
    }

    public void createNetwork() {
        this.networks.put(this.networkID++, new PipeNetwork());
        this.markDirty();
    }

    public void removeNetwork(int id) {
        this.networks.remove(id);
        this.markDirty();
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {

    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        NBTTagList networkList = new NBTTagList();
        for (Int2ObjectMap.Entry<PipeNetwork> entry : this.networks.int2ObjectEntrySet()) {
            NBTTagCompound networkData = new NBTTagCompound();
            networkData.setInteger(TAG_ID, entry.getIntKey());
            networkData.setTag(TAG_NETWORK, entry.getValue().serializeNBT());
            networkList.appendTag(networkData);
        }
        nbt.setTag(TAG_NETWORK_LIST, networkList);
        nbt.setInteger(TAG_NEXT_ID, this.networkID);

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
