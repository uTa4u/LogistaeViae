package su.uTa4u.logistaeviae.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.Constants;
import su.uTa4u.logistaeviae.block.BlockSimplePipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;

public class TileEntitySimplePipe extends TileEntity {
    public static final String TAG_CONNECTIONS = "Connections";

    private final Set<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class);

    private static final Random RNG = new Random(1);
    private static final Item[] TEST_ITEMS = new Item[]{Items.APPLE, Items.BREAD, Item.getItemFromBlock(Blocks.SAND), Item.getItemFromBlock(Blocks.STONE)};
    public final Item item;

    public TileEntitySimplePipe() {
        super();
        this.item = TEST_ITEMS[RNG.nextInt(TEST_ITEMS.length)];
    }

    public void tryConnect(@Nonnull EnumFacing facing) {
        if (canConnectTo(facing)) {
            this.connections.add(facing);
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

    private boolean canConnectTo(@Nonnull EnumFacing facing) {
        Block otherBlock = this.world.getBlockState(this.pos.offset(facing)).getBlock();
        return otherBlock instanceof BlockSimplePipe;
    }

    public void forEachConnection(Consumer<EnumFacing> consumer) {
        this.connections.forEach(consumer);
    }

    @Override
    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        byte data = 0;
        for (EnumFacing facing : this.connections) {
            data |= (byte) (1 << facing.getIndex());
        }
        nbt.setByte(TAG_CONNECTIONS, data);

        return nbt;
    }

    @Override
    public void readFromNBT(@Nonnull NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.connections.clear();
        byte data = nbt.getByte(TAG_CONNECTIONS);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (((data >> facing.getIndex()) & 1) == 1) {
                this.connections.add(facing);
            }
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
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }
}
