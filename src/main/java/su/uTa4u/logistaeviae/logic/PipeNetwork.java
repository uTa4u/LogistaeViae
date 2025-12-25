package su.uTa4u.logistaeviae.logic;

import net.minecraft.util.math.BlockPos;
import su.uTa4u.logistaeviae.block.BlockPipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PipeNetwork {

    private final Map<BlockPipe, Set<BlockPos>> PIPE_POSITIONS = new HashMap<>();

    PipeNetwork() {

    }

    public PipeRoute getRoute(BlockPos pos1, BlockPos pos2) {
        return null;
    }
}
