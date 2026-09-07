package su.uTa4u.logistaeviae.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import su.uTa4u.logistaeviae.block.BlockPipe;
import su.uTa4u.logistaeviae.logic.type.OrderPlacer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/*
    TODO list for the mod in general:
        1. Add all missing blocks/items/guis
        2. Make network data structure easily changeable for testing
        3. Move pathfinding algorithm from PipeRoute
        4. Implement OrderTable and order fullfilment algorithm
        5. Implement pipe logic in a form of Functional Interfaces
        6. Implement smart logic for ticking the network
        7. Make network require energy
 */
public class TileEntityPipe extends TileEntity {
    public static final String TAG_CONNECTIONS = "Connections";
    public static final String TAG_ITEMS = "Items";

    // TODO: maybe store in byte form
    private final EnumSet<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class);
    // TODO: not hardcode size ofc
    private final ItemStackHandler items = new ItemStackHandler(9) {
        @Override
        protected void onContentsChanged(int slot) {
            TileEntityPipe.this.markDirty();
        }
    };
    @Nonnull
    private final List<OrderPlacer> orderPlacers;

    // Required for TE to be correctly loaded
    public TileEntityPipe() {
        this(Collections.emptyList());
    }

    public TileEntityPipe(@Nonnull List<OrderPlacer> orderPlacers) {
        super();
        this.orderPlacers = orderPlacers;
    }

    public void forEachOrderPlacer() {
        for (OrderPlacer p : this.orderPlacers) {
            if (p != null) {
                p.place();
            }
        }
    }

    public ItemStackHandler getItems() {
        return this.items;
    }

    @Override
    @Nonnull
    public BlockPipe getBlockType() {
        Block block = super.getBlockType();
        if (!(block instanceof BlockPipe)) throw new RuntimeException("TileEntityPipe is not BlockPipe");
        return (BlockPipe) block;
    }

    public boolean canConnect(@Nullable TileEntity te, EnumFacing facing) {
        return te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
    }

    public void connect(@Nonnull EnumFacing facing) {
        if (this.connections.add(facing)) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, Constants.BlockFlags.DEFAULT);
        }
    }

    public void disconnect(@Nonnull EnumFacing facing) {
        if (this.connections.remove(facing)) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, Constants.BlockFlags.DEFAULT);
        }
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setByte(TAG_CONNECTIONS, this.packConnections());

        nbt.setTag(TAG_ITEMS, this.items.serializeNBT());

        return nbt;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.connections.clear();
        this.connections.addAll(unpackConnections(nbt.getByte(TAG_CONNECTIONS)));

        if (nbt.hasKey(TAG_ITEMS)) {
            this.items.deserializeNBT((NBTTagCompound) nbt.getTag(TAG_ITEMS));
        }
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 1, this.getUpdateTag());
    }

    @Override
    @Nonnull
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(@Nonnull NetworkManager net, @Nonnull SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
        this.world.markBlockRangeForRenderUpdate(this.pos, this.pos);
    }

    public EnumSet<EnumFacing> getConnections() {
        return this.connections;
    }

    public byte packConnections() {
        byte data = 0;
        for (EnumFacing facing : this.connections) {
            data |= (byte) (1 << facing.getIndex());
        }
        return data;
    }

    // TODO: make this a setter for connections field
    public static Set<EnumFacing> unpackConnections(byte packedConnections) {
        Set<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (((packedConnections >> facing.getIndex()) & 1) == 1) {
                connections.add(facing);
            }
        }
        return connections;
    }

    @Nullable
    public static TileEntityPipe getOrNull(TileEntity te) {
        if (te instanceof TileEntityPipe) return (TileEntityPipe) te;
        return null;
    }
}
