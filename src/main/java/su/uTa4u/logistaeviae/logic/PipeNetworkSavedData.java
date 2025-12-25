package su.uTa4u.logistaeviae.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import su.uTa4u.logistaeviae.Tags;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PipeNetworkSavedData extends WorldSavedData {
    private static final String NAME = Tags.MOD_ID + "_PipeNetworkSavedData";

    private final Set<PipeNetwork> PIPE_NETWORKS = new HashSet<>();

    public PipeNetworkSavedData() {
        this(NAME);
    }

    public PipeNetworkSavedData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {

    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        return null;
    }

    public static PipeNetworkSavedData get(World world) {
        // TODO: Maybe it should be per-world storage?
        MapStorage storage = Objects.requireNonNull(world.getMapStorage());
        PipeNetworkSavedData instance = (PipeNetworkSavedData) storage.getOrLoadData(PipeNetworkSavedData.class, NAME);
        if (instance == null) {
            instance = new PipeNetworkSavedData();
            storage.setData(NAME, instance);
        }
        return instance;
    }
}
