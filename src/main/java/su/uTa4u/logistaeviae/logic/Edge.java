package su.uTa4u.logistaeviae.logic;

import net.minecraft.util.math.BlockPos;

// It may have zero intermediate pipe blocks (directly adjacent nodes)
// or many (a compressed corridor of pass-through pipes).
public final class Edge {
    public final Node start;
    public final Node end;
    public final BlockPos[] pipeBlocks;
    // Doesn't count the endpoints
    public final int length;

    public Edge(Node start, Node end, BlockPos[] pipeBlocks) {
        this.start = start;
        this.end = end;
        this.pipeBlocks = pipeBlocks;
        this.length = pipeBlocks.length;
    }
}
