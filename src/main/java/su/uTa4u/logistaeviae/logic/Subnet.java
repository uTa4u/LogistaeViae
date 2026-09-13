package su.uTa4u.logistaeviae.logic;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import su.uTa4u.logistaeviae.block.BlockPipe;
import su.uTa4u.logistaeviae.tileentity.TileEntityPipe;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class Subnet {
    public static final int SUBNET_SHIFT = 4;
    public static final int SUBNET_MASK = (1 << SUBNET_SHIFT) - 1;
    public static final int COORD_BITS = SUBNET_SHIFT;
    public static final int NODE_ID_BITS = COORD_BITS * 3;

    private static final int MAX_CORRIDOR = 4096;
    private static final int REBUILD_COOLDOWN = 10;

    public final int cx;
    public final int cy;
    public final int cz;
    private final Map<BlockPos, Node> nodeMap = new HashMap<>();
    private final List<Edge> edges = new ArrayList<>();
    private final List<Node> portals = new ArrayList<>();
    private final PathCache pathCache = new PathCache();

    private boolean loaded = true;
    private boolean dirty;
    private int cooldownTicks = 0;

    public Subnet(int cx, int cy, int cz) {
        this.cx = cx;
        this.cy = cy;
        this.cz = cz;
    }

    public void markDirty() {
        this.dirty = true;
        this.cooldownTicks = REBUILD_COOLDOWN;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void tick(World world) {
        if (!this.loaded) return;
        if (!this.dirty) return;
        if (--this.cooldownTicks > 0) return;

        this.nodeMap.clear();
        this.edges.clear();
        this.portals.clear();
        this.pathCache.clear();
        discoverNodes(world);
        compressEdges(world);
        identifyPortals();
        buildNeighborReferences();

        this.dirty = false;
    }

    // TODO: use Dijksta or even A* instead of BFS
    public List<Edge> findPath(BlockPos startPos, BlockPos endPos) {
        Node start = this.nodeMap.get(startPos);
        Node end = this.nodeMap.get(endPos);
        if (start == null || end == null) {
            return Collections.emptyList();
        }

        long cacheKey = directedKey(start, end);
        List<Edge> cached = this.pathCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Queue<Node> queue = new ArrayDeque<>();
        Map<Node, Node> predecessors = new HashMap<>();
        Map<Node, Edge> edgeToPredecessor = new HashMap<>();
        queue.add(start);
        predecessors.put(start, null);

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current == end) break;
            for (EnumFacing dir : EnumFacing.VALUES) {
                Edge edge = current.getEdge(dir);
                if (edge == null) continue;
                Node next = (edge.start == current) ? edge.end : edge.start;
                if (!predecessors.containsKey(next)) {
                    predecessors.put(next, current);
                    edgeToPredecessor.put(next, edge);
                    queue.add(next);
                }
            }
        }

        if (!predecessors.containsKey(end)) {
            List<Edge> empty = Collections.emptyList();
            this.pathCache.put(cacheKey, empty);
            return empty;
        }

        List<Edge> path = new ArrayList<>();
        Node step = end;
        while (step != null && predecessors.get(step) != null) {
            path.add(edgeToPredecessor.get(step));
            step = predecessors.get(step);
        }
        Collections.reverse(path);
        this.pathCache.put(cacheKey, path);
        return path;
    }

    private void discoverNodes(World world) {
        int baseX = this.cx << 4;
        int baseY = this.cy << 4;
        int baseZ = this.cz << 4;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int lx = 0; lx < 16; lx++) {
            for (int ly = 0; ly < 16; ly++) {
                for (int lz = 0; lz < 16; lz++) {
                    cursor.setPos(baseX + lx, baseY + ly, baseZ + lz);
                    if (!isPipe(world, cursor)) continue;

                    int pipeNeighbors = 0;
                    boolean canConnect = false;
                    for (EnumFacing facing : EnumFacing.VALUES) {
                        BlockPos pos = cursor.offset(facing);
                        if (isPipe(world, pos)) {
                            pipeNeighbors++;
                        } else if (TileEntityPipe.canConnect(world.getTileEntity(pos), facing.getOpposite())) {
                            canConnect = true;
                        }
                    }

                    boolean exitsSubnet =
                            (lx == 0 && isPipe(world, cursor.west())) ||
                                    (lx == 15 && isPipe(world, cursor.east())) ||
                                    (ly == 0 && isPipe(world, cursor.down())) ||
                                    (ly == 15 && isPipe(world, cursor.up())) ||
                                    (lz == 0 && isPipe(world, cursor.north())) ||
                                    (lz == 15 && isPipe(world, cursor.south()));

                    if (pipeNeighbors > 2 || canConnect || exitsSubnet) {
                        Node node = new Node(cursor.toImmutable());
                        if (pipeNeighbors != 2) {
                            node.setIntersection(true);
                        }
                        if (canConnect) {
                            node.setInventory(true);
                        }
                        this.nodeMap.put(node.getPos(), node);
                    }
                }
            }
        }
    }

    private void compressEdges(World world) {
        this.edges.clear();
        Set<Long> seen = new HashSet<>();

        for (Node node : this.nodeMap.values()) {
            for (EnumFacing dir : EnumFacing.VALUES) {
                BlockPos first = node.getPos().offset(dir);
                if (!isPipeAt(world, first)) continue;

                Node direct = this.nodeMap.get(first);
                if (direct != null) {
                    addEdge(node, direct, new BlockPos[0], seen);
                    continue;
                }

                List<BlockPos> corridor = new ArrayList<>();
                corridor.add(first);
                BlockPos current = first;
                EnumFacing cameFrom = dir;
                Node target = null;

                while (corridor.size() < MAX_CORRIDOR) {
                    EnumFacing nextDir = otherPipeDir(world, current, cameFrom);
                    if (nextDir == null) break;

                    BlockPos next = current.offset(nextDir);
                    Node n = this.nodeMap.get(next);
                    if (n != null) {
                        target = n;
                        break;
                    }
                    if (!isPipeAt(world, next)) break;

                    corridor.add(next);
                    current = next;
                    cameFrom = nextDir;
                }

                if (target != null) {
                    addEdge(node, target, corridor.toArray(new BlockPos[0]), seen);
                }
            }
        }
    }

    private boolean isPipeAt(World world, BlockPos pos) {
        return ((pos.getX() >> 4) == this.cx || (pos.getY() >> 4) == this.cy || (pos.getZ() >> 4) == this.cz) && isPipe(world, pos);
    }

    private EnumFacing otherPipeDir(World world, BlockPos pos, EnumFacing cameFrom) {
        EnumFacing back = cameFrom.getOpposite();
        for (EnumFacing dir : EnumFacing.VALUES) {
            if (dir == back) continue;
            if (isPipeAt(world, pos.offset(dir))) return dir;
        }
        return null;
    }

    private void addEdge(Node a, Node b, BlockPos[] blocks, Set<Long> seen) {
        long key = undirectedKey(a, b);
        if (seen.add(key)) {
            this.edges.add(new Edge(a, b, blocks));
        }
    }

    private void identifyPortals() {
        for (Node node : this.nodeMap.values()) {
            BlockPos pos = node.getPos();
            int x = pos.getX() & SUBNET_MASK;
            int y = pos.getY() & SUBNET_MASK;
            int z = pos.getZ() & SUBNET_MASK;
            if (x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15) {
                node.setPortal(true);
                this.portals.add(node);
            }
        }
    }

    private void buildNeighborReferences() {
        for (Node node : this.nodeMap.values()) {
            node.clearEdges();
        }

        for (Edge edge : this.edges) {
            EnumFacing fromStart = exitFacing(edge.start, edge);
            EnumFacing fromEnd = exitFacing(edge.end, edge);
            if (fromStart != null) {
                edge.start.setEdge(fromStart, edge);
            }
            if (fromEnd != null) {
                edge.end.setEdge(fromEnd, edge);
            }
        }
    }

    private static EnumFacing exitFacing(Node from, Edge edge) {
        BlockPos toward;
        if (edge.pipeBlocks.length > 0) {
            toward = (from == edge.start)
                    ? edge.pipeBlocks[0]
                    : edge.pipeBlocks[edge.pipeBlocks.length - 1];
        } else {
            toward = (from == edge.start)
                    ? edge.end.getPos()
                    : edge.start.getPos();
        }
        return directionBetween(from.getPos(), toward);
    }

    private static EnumFacing directionBetween(BlockPos from, BlockPos to) {
        for (EnumFacing f : EnumFacing.VALUES) {
            if (from.offset(f).equals(to)) return f;
        }
        return null;
    }

    private static boolean isPipe(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() instanceof BlockPipe;
    }

    private static long undirectedKey(Node a, Node b) {
        int ia = a.getId();
        int ib = b.getId();
        if (ia > ib) {
            int t = ia;
            ia = ib;
            ib = t;
        }
        return (((long) ia) << NODE_ID_BITS) | ib;
    }

    private static long directedKey(Node start, Node end) {
        return (((long) start.getId()) << NODE_ID_BITS) | end.getId();
    }

}
