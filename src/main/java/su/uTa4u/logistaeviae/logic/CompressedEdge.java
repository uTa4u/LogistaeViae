package su.uTa4u.logistaeviae.logic;

import net.minecraft.util.math.BlockPos;

public class CompressedEdge {
    public final Node start;
    public final Node end;
    public final int length;
    public final BlockPos[] pipeBlocks;

    public CompressedEdge(Node start, Node end, int length, BlockPos[] pipeBlocks) {
        this.start = start;
        this.end = end;
        this.length = length;
        this.pipeBlocks = pipeBlocks;
    }
}
