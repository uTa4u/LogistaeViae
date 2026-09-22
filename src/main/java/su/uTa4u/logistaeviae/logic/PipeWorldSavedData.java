package su.uTa4u.logistaeviae.logic;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import su.uTa4u.logistaeviae.Tags;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public final class PipeWorldSavedData extends WorldSavedData {
    private static final String TAG_SUBNETS = "Subnets";
    private static final String NAME = Tags.MOD_ID + "_PipeWorldSavedData";

    // Minecraft's world size is +-30_000_000, subnet has a size of 16,
    // +-30_000_000 / 16 = +-1_875_000 for which 21 bit (+-2_097_151) is the minimum to cover the whole world.
    private static final int SUBNET_COORD_BITS = 21;
    private static final int SUBNET_COORD_MASK = (1 << SUBNET_COORD_BITS) - 1;
    private static final int X_SHIFT = SUBNET_COORD_BITS * 2;
    private static final int Y_SHIFT = SUBNET_COORD_BITS;
    private static final int Z_SHIFT = 0;

    private final Long2ObjectMap<Subnet> subnets = new Long2ObjectOpenHashMap<>();

    public PipeWorldSavedData(String ignored) {
        super(NAME);
    }

    // TODO: make these numbers not magic constants, also below
    private long pack(int cx, int cy, int cz) {
        return (((long) cx & SUBNET_COORD_MASK) << X_SHIFT)
                | (((long) cy & SUBNET_COORD_MASK) << Y_SHIFT)
                | (((long) cz & SUBNET_COORD_MASK) << Z_SHIFT);
    }

    private long pack(BlockPos pos) {
        return pack(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
    }

    @Nullable
    Subnet getSubnet(BlockPos pos) {
        return this.subnets.get(pack(pos));
    }

    @Nullable
    Subnet getSubnet(int cx, int cy, int cz) {
        return this.subnets.get(pack(cx, cy, cz));
    }

    public Subnet getOrCreateSubnet(BlockPos pos) {
        long key = pack(pos);
        Subnet subnet = this.subnets.get(key);
        if (subnet == null) {
            subnet = new Subnet(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
            this.subnets.put(key, subnet);
            markDirty();
        }
        return subnet;
    }

    void tickAll(World world) {
        Iterator<Map.Entry<Long, Subnet>> it = this.subnets.entrySet().iterator();
        boolean changed = false;
        while (it.hasNext()) {
            if (it.next().getValue().tick(world)) {
                it.remove();
                changed = true;
            }
        }
        if (changed) {
            markDirty();
        }
    }

    void onChunkLoad(int cx, int cy, int cz) {
        Subnet subnet = this.getSubnet(cx, cy, cz);
        if (subnet != null) {
            subnet.setLoaded(true);
        }
    }

    void onChunkUnload(int cx, int cy, int cz) {
        Subnet subnet = this.getSubnet(cx, cy, cz);
        if (subnet != null) {
            subnet.setLoaded(false);
        }
    }

    private void markSubnetDirty(BlockPos pos) {
        Subnet subnet = this.getSubnet(pos);
        if (subnet != null) {
            subnet.markDirty();
        }
    }

    public void markSubnetAndBoundaryDirty(BlockPos pos) {
        markSubnetDirty(pos);
        int lx = pos.getX() & Subnet.SUBNET_MASK;
        int ly = pos.getY() & Subnet.SUBNET_MASK;
        int lz = pos.getZ() & Subnet.SUBNET_MASK;
        if (lx == 0) markSubnetDirty(pos.west());
        if (lx == 15) markSubnetDirty(pos.east());
        if (ly == 0) markSubnetDirty(pos.down());
        if (ly == 15) markSubnetDirty(pos.up());
        if (lz == 0) markSubnetDirty(pos.north());
        if (lz == 15) markSubnetDirty(pos.south());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.subnets.clear();
        NBTTagList list = nbt.getTagList(TAG_SUBNETS, Constants.NBT.TAG_LONG);
        for (NBTBase tag : list) {
            if (!(tag instanceof NBTTagLong)) continue;

            long key = ((NBTTagLong) tag).getLong();
            int cx = (int) ((key >> X_SHIFT) & SUBNET_COORD_MASK);
            int cy = (int) ((key >> Y_SHIFT) & SUBNET_COORD_MASK);
            int cz = (int) ((key >> Z_SHIFT) & SUBNET_COORD_MASK);

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
        compound.setTag(TAG_SUBNETS, list);
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
