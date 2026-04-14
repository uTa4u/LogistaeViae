package su.uTa4u.logistaeviae.logic;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

// TODO: add all missing items/blocks from LP
public final class PipeNetwork {
    private static final String TAG_ID = "Id";
    private static final String TAG_PIPES = "Pipes";

    private final Map<PipeLocation, Map<PipeLocation, PipeRoute>> routeCache = new HashMap<>();
    private final Set<PipeLocation> pipes = new HashSet<>();
    private final PipeNetworkSavedData savedData;
    private int networkID;

    PipeNetwork(int networkID, PipeNetworkSavedData savedData) {
        this.networkID = networkID;
        this.savedData = savedData;
    }

    public void add(int dim, BlockPos pos) {
        PipeLocation pipeLoc = new PipeLocation(dim, pos);
        this.pipes.add(pipeLoc);
        this.savedData.putIdByPos(pipeLoc, this.networkID);
        this.savedData.markDirty();
    }

    public void remove(int dim, BlockPos pos) {
        PipeLocation pipeLoc = new PipeLocation(dim, pos);
        this.pipes.remove(pipeLoc);
        this.savedData.removeIdByPos(pipeLoc);
        this.savedData.markDirty();
    }

    public void merge(PipeNetwork that) {
        if (this == that) return;
        this.pipes.addAll(that.pipes);
        this.savedData.removeNetwork(that.networkID);
        this.savedData.markDirty();
    }

    public void forEachPipe(Consumer<PipeLocation> consumer) {
        this.pipes.forEach(consumer);
    }

    @Nullable
    public PipeRoute getRoute(int dim, BlockPos pos1, BlockPos pos2) {
        return this.getRoute(dim, pos1, dim, pos2);
    }

    @Nullable
    public PipeRoute getRoute(int dim1, BlockPos pos1, int dim2, BlockPos pos2) {
        return this.getRoute(new PipeLocation(dim1, pos1), new PipeLocation(dim2, pos2));
    }

    @Nullable
    public PipeRoute getRoute(PipeLocation pipeLoc1, PipeLocation pipeLoc2) {
        if (pipeLoc1.pos == null || pipeLoc2.pos == null) return null;
        // TODO: Maybe routes in the same block should be allowed (Chassie Pipes from LP)
        if (pipeLoc1.dim == pipeLoc2.dim && pipeLoc1.pos.equals(pipeLoc2.pos)) return null;

        Map<PipeLocation, PipeRoute> from1Map = this.routeCache.get(pipeLoc1);
        if (from1Map != null) {
            PipeRoute route = from1Map.get(pipeLoc2);
            if (route != null) return route;
        }

        Map<PipeLocation, PipeRoute> from2Map = this.routeCache.get(pipeLoc2);
        if (from2Map != null) {
            PipeRoute route = from2Map.get(pipeLoc1);
            if (route != null) return route;
        }

        PipeRoute route = PipeRoute.compute(pipeLoc1, pipeLoc2);
        if (route != null) {

        }

        return null;
    }

    void readFromNBT(NBTTagCompound nbt) {
        this.pipes.clear();
        NBTTagList pipesTagList = nbt.getTagList(TAG_PIPES, 10);
        for (int i = 0; i < pipesTagList.tagCount(); i++) {
            this.pipes.add(new PipeLocation(pipesTagList.getCompoundTagAt(i)));
        }

        this.networkID = nbt.getInteger(TAG_ID);
        if (this.networkID == 0) this.networkID = 1;
    }

    @Nonnull
    NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList pipesTagList = new NBTTagList();
        for (PipeLocation pipeLoc : this.pipes) {
            pipesTagList.appendTag(pipeLoc.serializeNBT());
        }
        nbt.setTag(TAG_PIPES, pipesTagList);

        nbt.setInteger(TAG_ID, this.networkID);

        return nbt;
    }
}
