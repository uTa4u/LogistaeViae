package su.uTa4u.logistaeviae.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;
import su.uTa4u.logistaeviae.client.model.PipeModelManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

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

    public TileEntityPipe() {
        super();
    }

    public ItemStackHandler getItems() {
        return this.items;
    }

    public boolean canConnect(TileEntity te) {
        return false;
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
