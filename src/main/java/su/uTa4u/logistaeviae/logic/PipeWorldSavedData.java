package su.uTa4u.logistaeviae.logic;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import su.uTa4u.logistaeviae.Tags;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class PipeWorldSavedData extends WorldSavedData {

    private static final String NAME = Tags.MOD_ID + "_PipeWorldSavedData";

    private final Long2ObjectMap<Subnet> subnets = new Long2ObjectOpenHashMap<>();

    public PipeWorldSavedData(String ignored) {
        super(NAME);
    }

    private long pack(int cx, int cy, int cz) {
        return (((long) cx & 0x1FFFFF) << 42) |
                (((long) cy & 0x1FFFFF) << 21) |
                ((long) cz & 0x1FFFFF);
    }

    Subnet getSubnet(BlockPos pos) {
        return this.subnets.get(pack(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4));
    }

    Subnet getOrCreateSubnet(BlockPos pos) {
        long key = pack(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
        Subnet existing = this.subnets.get(key);
        if (existing != null) return existing;
        Subnet subnet = new Subnet(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
        this.subnets.put(key, subnet);
        markDirty();
        return subnet;
    }

    Subnet getAdjacentSubnet(Subnet subnet, EnumFacing facing) {
        return this.subnets.get(pack(
                subnet.cx + facing.getXOffset(),
                subnet.cy + facing.getYOffset(),
                subnet.cz + facing.getZOffset()
        ));
    }

    void removeSubnet(BlockPos pos) {
        if (this.subnets.remove(pack(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4)) != null) {
            markDirty();
        }
    }

    void markSubnetDirty(BlockPos pos) {
        Subnet subnet = getSubnet(pos);
        if (subnet != null) subnet.markDirty();
    }

    void tickAll(World world) {
        for (Subnet subnet : this.subnets.values()) {
            subnet.tick(world);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.subnets.clear();
        NBTTagList list = nbt.getTagList("subnets", Constants.NBT.TAG_LONG);
        for (NBTBase tag : list) {
            if (!(tag instanceof NBTTagLong)) continue;

            long key = ((NBTTagLong) tag).getLong();
            int cx = (int) ((key >> 21 * 2) & 0x1FFFFF);
            int cy = (int) ((key >> 21 * 1) & 0x1FFFFF);
            int cz = (int) ((key >> 21 * 0) & 0x1FFFFF);

            Subnet subnet = new Subnet(cx, cy, cz);
            subnet.markDirty();
            this.subnets.put(key, subnet);
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (long key : this.subnets.keySet()) {
            list.appendTag(new NBTTagLong(key));
        }
        compound.setTag("subnets", list);
        return compound;
    }

    @Nonnull
    public static PipeWorldSavedData get(World world) {
        MapStorage storage = Objects.requireNonNull(world.getMapStorage());
        PipeWorldSavedData instance = (PipeWorldSavedData) storage.getOrLoadData(PipeWorldSavedData.class, NAME);
        if (instance == null) {
            instance = new PipeWorldSavedData(NAME);
            storage.setData(NAME, instance);
        }
        return instance;
    }
}
