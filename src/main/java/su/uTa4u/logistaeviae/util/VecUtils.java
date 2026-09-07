package su.uTa4u.logistaeviae.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public final class VecUtils {
    private VecUtils() {
    }

    public static EnumFacing getFacingFromNeighbouringPos(BlockPos pos1, BlockPos pos2) {
        BlockPos diff = pos2.subtract(pos1);
        for (EnumFacing f : EnumFacing.VALUES) {
            if (diff.compareTo(f.getDirectionVec()) == 0) {
                return f;
            }
        }
        throw new AssertionError("Positions aren't neighboring! pos1: " + pos1 + " pos2: " + pos2);
    }
}
