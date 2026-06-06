package su.uTa4u.logistaeviae.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
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
    private final Set<PipeLocation> teleportPipes = new HashSet<>();
    private final Map<PipeLocation, EnumSet<EnumFacing>> pipeConnections = new HashMap<>();
    private final PipeNetworkSavedData savedData;
    public final int networkID;

    PipeNetwork(int networkID, PipeNetworkSavedData savedData) {
        this.networkID = networkID;
        this.savedData = savedData;
    }

    PipeNetwork(NBTTagCompound nbt, PipeNetworkSavedData savedData) {
        this(validateNetworkId(nbt.getInteger(TAG_ID)), savedData);

        NBTTagList pipesTagList = nbt.getTagList(TAG_PIPES, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < pipesTagList.tagCount(); i++) {
            this.pipes.add(new PipeLocation(pipesTagList.getCompoundTagAt(i)));
        }
    }

    @Nonnull
    NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList pipesTagList = new NBTTagList();
        for (PipeLocation pipeLoc : this.pipes) {
            pipesTagList.appendTag(pipeLoc.serializeNBT());
        }
        nbt.setTag(TAG_PIPES, pipesTagList);

        nbt.setInteger(TAG_ID, this.networkID);

        return nbt;
    }

    public void add(PipeLocation pipeLoc, EnumSet<EnumFacing> connections) {
        this.pipes.add(pipeLoc);
        this.pipeConnections.put(pipeLoc, connections);
        this.savedData.putIdByPos(pipeLoc, this.networkID);
        this.savedData.markDirty();
    }

    public void remove(PipeLocation pipeLoc) {
        this.pipes.remove(pipeLoc);
        this.pipeConnections.remove(pipeLoc);
        this.savedData.removeIdByPos(pipeLoc);
        this.savedData.markDirty();
        if (this.pipes.isEmpty()) {
            this.savedData.removeNetwork(this.networkID);
        }
    }

    public void merge(PipeNetwork that) {
        if (that == null) return;
        if (this == that) return;
        this.pipes.addAll(that.pipes);
        this.pipeConnections.putAll(that.pipeConnections);
        that.pipes.forEach((pipeLoc) -> {
            this.savedData.removeIdByPos(pipeLoc);
            this.savedData.putIdByPos(pipeLoc, this.networkID);
        });
        this.savedData.removeNetwork(that.networkID);
        this.savedData.markDirty();
    }

    public Set<PipeLocation> getNeighbours(PipeLocation pipeLoc) {
        Set<PipeLocation> connections = new HashSet<>();
        this.pipeConnections.get(pipeLoc).forEach(dir -> {
            PipeLocation neighbourLoc = new PipeLocation(pipeLoc.dim, pipeLoc.pos.offset(dir));
            if (this.pipes.contains(neighbourLoc)) connections.add(neighbourLoc);
        });
        return connections;
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
    public PipeRoute getRoute(PipeLocation from, PipeLocation to) {
        if (from == null || to == null) return null;
        if (from.pos == null || to.pos == null) return null;
        // TODO: Maybe routes in the same block should be allowed (Chassie Pipes from LP)
        if (from.dim == to.dim && from.pos.equals(to.pos)) return null;

        Map<PipeLocation, PipeRoute> fromMap = this.routeCache.computeIfAbsent(from, i -> new HashMap<>());
        PipeRoute route = fromMap.get(to);
        if (route != null) return route;

        route = PipeRoute.compute(this, from, to);
        if (route != null) {
            fromMap.put(to, route);
            return route;
        }

        return null;
    }

    @Nullable
    public PipeLocation findNearestTeleportPipe(PipeLocation loc) {
        PipeLocation ret = null;
        int dist = Integer.MAX_VALUE;
        for (PipeLocation tpLoc : this.teleportPipes) {
            int d = loc.manhattanDistance(tpLoc);
            if (ret == null || d < dist) {
                ret = tpLoc;
                dist = d;
            }
        }
        return ret;
    }

    private static int validateNetworkId(int id) {
        return id == 0 ? 1 : id;
    }
}
