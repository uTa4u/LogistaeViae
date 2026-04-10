package su.uTa4u.logistaeviae.logic;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import su.uTa4u.logistaeviae.Tags;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class PipeNetworkSavedData extends WorldSavedData {
    private static final String TAG_ID = "Id";
    private static final String TAG_NETWORK = "Network";
    private static final String TAG_NETWORK_LIST = "Networks";
    private static final String TAG_NEXT_ID = "NextId";
    private static final String NAME = Tags.MOD_ID + "_PipeNetworkSavedData";

    // TODO: maybe try using UUID as key
    private final Int2ObjectMap<PipeNetwork> networks = new Int2ObjectOpenHashMap<>();
    // FIXME: save this to nbt
    private final Object2IntMap<BlockPos> idByPos = new Object2IntOpenHashMap<>();
    private int networkNextID = 1; // 0 is default return value for fastutil maps

    public PipeNetworkSavedData() {
        super(NAME);
    }

    public PipeNetworkSavedData(String s) {
        super(s);
    }

    public PipeNetwork getNetwork(int id) {
        return this.networks.get(id);
    }

    public PipeNetwork getNetwork(BlockPos pos) {
        return this.getNetwork(this.idByPos.getInt(pos));
    }

    public void createNetwork(int dim, BlockPos pos) {
        PipeNetwork network = new PipeNetwork(this.networkNextID, this);
        network.add(dim, pos);
        this.networks.put(this.networkNextID, network);
        this.networkNextID++;
        this.markDirty();
    }

    public void removeNetwork(int id) {
        this.networks.remove(id).forEach(this.idByPos::remove);
        this.markDirty();
    }

    void putIdByPos(BlockPos pos, int id) {
        this.idByPos.put(pos, id);
    }

    void removeIdByPos(BlockPos pos) {
        this.idByPos.remove(pos);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        this.networks.clear();
        NBTTagList networkList = nbt.getTagList(TAG_NETWORK_LIST, 10);
        for (int i = 0; i < networkList.tagCount(); i++) {
            NBTTagCompound networkTag = networkList.getCompoundTagAt(i);
            int id = networkTag.getInteger(TAG_ID);
            PipeNetwork network = new PipeNetwork(id, this);
            network.readFromNBT(networkTag.getCompoundTag(TAG_NETWORK));
            this.networks.put(id, network);
        }

        this.networkNextID = nbt.getInteger(TAG_NEXT_ID);
        if (this.networkNextID == 0) this.networkNextID = 1;
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        NBTTagList networkList = new NBTTagList();
        for (Int2ObjectMap.Entry<PipeNetwork> entry : this.networks.int2ObjectEntrySet()) {
            NBTTagCompound networkData = new NBTTagCompound();
            networkData.setInteger(TAG_ID, entry.getIntKey());
            networkData.setTag(TAG_NETWORK, entry.getValue().writeToNBT(new NBTTagCompound()));
            networkList.appendTag(networkData);
        }
        nbt.setTag(TAG_NETWORK_LIST, networkList);

        nbt.setInteger(TAG_NEXT_ID, this.networkNextID);

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
