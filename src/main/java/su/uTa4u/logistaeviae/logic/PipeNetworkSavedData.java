package su.uTa4u.logistaeviae.logic;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import su.uTa4u.logistaeviae.Tags;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class PipeNetworkSavedData extends WorldSavedData {
    private static final String TAG_NETWORK_ID = "Id";
    private static final String TAG_NETWORK = "Network";
    private static final String TAG_NETWORK_LIST = "Networks";
    private static final String TAG_NEXT_ID = "NextId";
    private static final String TAG_ID_BY_POS = "IdByPos";
    private static final String TAG_PIPE_LOCATIONS = "PipeLocations";

    private static final String NAME = Tags.MOD_ID + "_PipeNetworkSavedData";

    private final Int2ObjectMap<PipeNetwork> networks = new Int2ObjectOpenHashMap<>();
    private final Object2IntMap<PipeLocation> idByPos = new Object2IntOpenHashMap<>();
    private int networkNextID = 1; // 0 is default return value for fastutil maps

    public PipeNetworkSavedData() {
        this(NAME);
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
        this.networks.remove(id).forEachPipe(this.idByPos::remove);
        this.markDirty();
    }

    void putIdByPos(PipeLocation pipeLoc, int id) {
        this.idByPos.put(pipeLoc, id);
    }

    void removeIdByPos(PipeLocation pipeLoc) {
        this.idByPos.remove(pipeLoc);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        this.networks.clear();
        NBTTagList networkList = nbt.getTagList(TAG_NETWORK_LIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < networkList.tagCount(); i++) {
            NBTTagCompound networkTag = networkList.getCompoundTagAt(i);
            int id = networkTag.getInteger(TAG_NETWORK_ID);
            PipeNetwork network = new PipeNetwork(id, this);
            network.readFromNBT(networkTag.getCompoundTag(TAG_NETWORK));
            this.networks.put(id, network);
        }

        this.idByPos.clear();
        NBTTagList idByPosList = nbt.getTagList(TAG_ID_BY_POS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < idByPosList.tagCount(); i++) {
            NBTTagCompound idData = idByPosList.getCompoundTagAt(i);
            int id = idData.getInteger(TAG_NETWORK_ID);
            NBTTagList pipeLocs = idData.getTagList(TAG_PIPE_LOCATIONS, Constants.NBT.TAG_COMPOUND);
            for (int j = 0; j < pipeLocs.tagCount(); j++) {
                this.idByPos.put(new PipeLocation(pipeLocs.getCompoundTagAt(j)), id);
            }
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
            networkData.setInteger(TAG_NETWORK_ID, entry.getIntKey());
            networkData.setTag(TAG_NETWORK, entry.getValue().writeToNBT(new NBTTagCompound()));
            networkList.appendTag(networkData);
        }
        nbt.setTag(TAG_NETWORK_LIST, networkList);

        NBTTagList idByPosList = new NBTTagList();
        Int2ObjectMap<NBTTagList> positionLists = new Int2ObjectArrayMap<>();
        for (Object2IntMap.Entry<PipeLocation> entry : this.idByPos.object2IntEntrySet()) {
            int id = entry.getIntValue();
            if (!positionLists.containsKey(id)) {
                positionLists.put(id, new NBTTagList());
            }
            positionLists.get(id).appendTag(entry.getKey().serializeNBT());
        }
        for (Int2ObjectMap.Entry<NBTTagList> entry : positionLists.int2ObjectEntrySet()) {
            NBTTagCompound idData = new NBTTagCompound();
            idData.setInteger(TAG_NETWORK_ID, entry.getIntKey());
            idData.setTag(TAG_PIPE_LOCATIONS, entry.getValue());
            idByPosList.appendTag(idData);
        }
        nbt.setTag(TAG_ID_BY_POS, idByPosList);

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
