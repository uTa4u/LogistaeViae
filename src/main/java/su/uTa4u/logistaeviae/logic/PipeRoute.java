package su.uTa4u.logistaeviae.logic;

import javax.annotation.Nullable;
import java.util.*;

public final class PipeRoute {
    // TODO: make adjustable through config?
    private static final double TELEPORT_COST = 100;
    private final List<PipeLocation> path;

    private PipeRoute(List<PipeLocation> path) {
        this.path = Collections.unmodifiableList(path);
    }

    @Nullable
    static PipeRoute compute(PipeNetwork pipeNetwork, PipeLocation from, PipeLocation to) {
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Map<PipeLocation, PathNode> allNodes = new HashMap<>();

        PathNode startNode = new PathNode(from, null, 0, getHeuristic(from, to));
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

                double tentativeG = current.g + getDistance(current.location, neighbor);

                PathNode neighborNode = allNodes.get(neighbor);
                if (neighborNode == null) {
                    neighborNode = new PathNode(neighbor, current, tentativeG, getHeuristic(neighbor, to));
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

    private static double getDistance(PipeLocation from, PipeLocation to) {
        return from.dim != to.dim ? TELEPORT_COST : 1;
    }

    private static double getHeuristic(PipeLocation from, PipeLocation to) {
        if (from.dim != to.dim) {
            return getCrossDimensionHeuristic(from, to);
        }

        return Math.abs(from.pos.getX() - to.pos.getX()) +
                Math.abs(from.pos.getY() - to.pos.getY()) +
                Math.abs(from.pos.getZ() - to.pos.getZ());
    }

    private static double getCrossDimensionHeuristic(PipeLocation from, PipeLocation to) {
        PipeLocation nearestFromTeleport = findNearestTeleportPipe(from);
        PipeLocation nearestToTeleport = findNearestTeleportPipe(to);

        if (nearestFromTeleport == null || nearestToTeleport == null) {
            return Double.MAX_VALUE;
        }

        double toTeleportCost = Math.abs(from.pos.getX() - nearestFromTeleport.pos.getX()) +
                Math.abs(from.pos.getY() - nearestFromTeleport.pos.getY()) +
                Math.abs(from.pos.getZ() - nearestFromTeleport.pos.getZ());

        double fromTeleportCost = Math.abs(to.pos.getX() - nearestToTeleport.pos.getX()) +
                Math.abs(to.pos.getY() - nearestToTeleport.pos.getY()) +
                Math.abs(to.pos.getZ() - nearestToTeleport.pos.getZ());

        return toTeleportCost + TELEPORT_COST + fromTeleportCost;
    }

    private static PipeLocation findNearestTeleportPipe(PipeLocation location) {
        return null;
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
        private double g;
        private double h;
        private double f;

        PathNode(PipeLocation location, PathNode cameFrom, double g, double h) {
            this.location = location;
            this.cameFrom = cameFrom;
            this.g = g;
            this.h = h;
            this.f = g + h;
        }

        @Override
        public int compareTo(PathNode other) {
            return Double.compare(this.f, other.f);
        }
    }
}
