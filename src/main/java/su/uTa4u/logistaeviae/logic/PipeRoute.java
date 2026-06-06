package su.uTa4u.logistaeviae.logic;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public final class PipeRoute {
    private static final int TELEPORT_COST = 100;
    private final List<PipeLocation> path;

    private PipeRoute(List<PipeLocation> path) {
        this.path = Collections.unmodifiableList(path);
    }

    @Nullable
    static PipeRoute compute(PipeNetwork pipeNetwork, PipeLocation from, PipeLocation to) {
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Map<PipeLocation, PathNode> allNodes = new HashMap<>();

        PathNode startNode = new PathNode(from, null, 0, getHeuristic(pipeNetwork, from, to));
        openSet.add(startNode);
        allNodes.put(from, startNode);

        Set<PipeLocation> closedSet = new HashSet<>();

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();

            if (current.location.equals(to)) {
                return buildRouteFromPath(current);
            }

            closedSet.add(current.location);

            for (PipeLocation neighbor : pipeNetwork.getNeighbours(current.location)) {
                if (closedSet.contains(neighbor)) continue;

                int tentativeG = current.g + getDistance(current.location, neighbor);

                PathNode neighborNode = allNodes.get(neighbor);
                if (neighborNode == null) {
                    neighborNode = new PathNode(neighbor, current, tentativeG, getHeuristic(pipeNetwork, neighbor, to));
                    allNodes.put(neighbor, neighborNode);
                    openSet.add(neighborNode);
                } else if (tentativeG < neighborNode.g) {
                    neighborNode.cameFrom = current;
                    neighborNode.g = tentativeG;
                    neighborNode.f = tentativeG + neighborNode.h;
                    openSet.remove(neighborNode);
                    openSet.add(neighborNode);
                }
            }
        }

        return null;
    }

    private static int getDistance(PipeLocation from, PipeLocation to) {
        return from.dim != to.dim ? TELEPORT_COST : 1;
    }

    private static int getHeuristic(PipeNetwork pipeNetwork, PipeLocation from, PipeLocation to) {
        if (from.dim == to.dim) return from.manhattanDistance(to);

        PipeLocation nearestFromTeleport = pipeNetwork.findNearestTeleportPipe(from);
        PipeLocation nearestToTeleport = pipeNetwork.findNearestTeleportPipe(to);

        if (nearestFromTeleport == null || nearestToTeleport == null) {
            return Integer.MAX_VALUE;
        }

        return from.manhattanDistance(nearestFromTeleport) + TELEPORT_COST + to.manhattanDistance(nearestToTeleport);
    }

    private static PipeRoute buildRouteFromPath(PathNode endNode) {
        List<PipeLocation> path = new ArrayList<>();
        PathNode current = endNode;

        while (current != null) {
            path.add(current.location);
            current = current.cameFrom;
        }

        Collections.reverse(path);
        return new PipeRoute(path);
    }

    private static class PathNode implements Comparable<PathNode> {
        private final PipeLocation location;
        private PathNode cameFrom;
        private int g;
        private final int h;
        private int f;

        PathNode(PipeLocation location, PathNode cameFrom, int g, int h) {
            this.location = location;
            this.cameFrom = cameFrom;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        @Override
        public int compareTo(PathNode other) {
            return Integer.compare(this.f, other.f);
        }
    }
}
