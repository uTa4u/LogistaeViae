package su.uTa4u.logistaeviae.tileentity;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import su.uTa4u.logistaeviae.block.BlockSimplePipe;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Set;

public class TileEntitySimplePipe extends TileEntity {
    public static final String TAG_CONNECTIONS = "Connections";

    private final Set<EnumFacing> connections = EnumSet.noneOf(EnumFacing.class);

    public void tryConnect(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing) {
        if (canConnectTo(world, pos, facing)) {
            this.connections.add(facing);
            this.markDirty();
        }
    }

    public void disconnect(@Nonnull EnumFacing facing) {
        if (this.connections.remove(facing)) {
            this.markDirty();
        }
    }

    private boolean canConnectTo(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing) {
        Block otherBlock = world.getBlockState(pos.offset(facing)).getBlock();
        return otherBlock instanceof BlockSimplePipe;
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

        byte data = nbt.getByte(TAG_CONNECTIONS);
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (((data << facing.getIndex()) & 1) == 1) {
                this.connections.add(facing);
            }
        }

    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }
}
