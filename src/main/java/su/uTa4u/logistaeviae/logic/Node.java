package su.uTa4u.logistaeviae.logic;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public final class Node {
    public final BlockPos pos;
    public final Edge[] edgeByDirection = new Edge[6];
    public final int id;
    public boolean isPortal;
    public boolean isInventory;
    public boolean isIntersection;

    public Node(BlockPos pos) {
        this.pos = pos;
        this.id = ((pos.getX() & Subnet.SUBNET_MASK) << Subnet.COORD_BITS * 2) |
                  ((pos.getY() & Subnet.SUBNET_MASK) << Subnet.COORD_BITS * 1) |
                  ((pos.getZ() & Subnet.SUBNET_MASK) << Subnet.COORD_BITS * 0);
    }

    public Node neighborIn(EnumFacing dir) {
        Edge e = this.edgeByDirection[dir.ordinal()];
        if (e == null) return null;
        return (e.start == this) ? e.end : e.start;
    }
}
