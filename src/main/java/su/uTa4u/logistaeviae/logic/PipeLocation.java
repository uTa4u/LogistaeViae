package su.uTa4u.logistaeviae.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public final class PipeLocation {
    private static final String TAG_DIMENSION = "Dimension";
    private static final String TAG_POSITION = "Position";

    final int dim;
    final BlockPos pos;

    PipeLocation(int dim, BlockPos pos) {
        this.dim = dim;
        this.pos = pos;
    }

    PipeLocation(NBTTagCompound nbt) {
        this(nbt.getInteger(TAG_DIMENSION), NBTUtil.getPosFromTag(nbt.getCompoundTag(TAG_POSITION)));
    }

    NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger(TAG_DIMENSION, this.dim);
        nbt.setTag(TAG_POSITION, NBTUtil.createPosTag(this.pos));
        return nbt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PipeLocation)) return false;
        PipeLocation that = (PipeLocation) o;
        return this.dim == that.dim && this.pos.equals(that.pos);
    }

    @Override
    public int hashCode() {
        return 31 * this.dim + this.pos.hashCode();
    }

    @Override
    public String toString() {
        return "[Dim: " + this.dim + ", x: " + this.pos.getX() + ", y: " + this.pos.getY() + ", z: " + this.pos.getZ() + "]";
    }

}
