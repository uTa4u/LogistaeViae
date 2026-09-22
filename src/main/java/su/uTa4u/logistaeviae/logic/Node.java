package su.uTa4u.logistaeviae.logic;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public final class Node {
    private final BlockPos pos;
    private final Edge[] edgeByDirection = new Edge[6];
    private final int id;
    private boolean isPortal;
    private boolean isInventory;
    private boolean isIntersection;

    public Node(BlockPos pos) {
        this.pos = pos;
        this.id = ((pos.getX() & Subnet.SUBNET_MASK) << Subnet.NODE_X_SHIFT)
                | ((pos.getY() & Subnet.SUBNET_MASK) << Subnet.NODE_Y_SHIFT)
                | ((pos.getZ() & Subnet.SUBNET_MASK) << Subnet.NODE_Z_SHIFT);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void clearEdges() {
        Arrays.fill(this.edgeByDirection, null);
    }

    public Edge getEdge(EnumFacing dir) {
        return this.edgeByDirection[dir.ordinal()];
    }

    public void setEdge(EnumFacing dir, Edge edge) {
        this.edgeByDirection[dir.ordinal()] = edge;
    }

    public int getId() {
        return this.id;
    }

    public boolean isPortal() {
        return this.isPortal;
    }

    public void setPortal(boolean portal) {
        this.isPortal = portal;
    }

    public boolean isInventory() {
        return this.isInventory;
    }

    public void setInventory(boolean inventory) {
        this.isInventory = inventory;
    }

    public boolean isIntersection() {
        return this.isIntersection;
    }

    public void setIntersection(boolean intersection) {
        this.isIntersection = intersection;
    }
}
