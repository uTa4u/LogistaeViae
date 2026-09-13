package su.uTa4u.logistaeviae.logic;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.*;

public class PathCache {
    private final Long2ObjectMap<List<CompressedEdge>> cache = new Long2ObjectOpenHashMap<>();

    public List<CompressedEdge> get(long key) {
        return this.cache.get(key);
    }

    public void put(long key, List<CompressedEdge> path) {
        this.cache.put(key, Collections.unmodifiableList(new ArrayList<>(path)));
    }

    public void clear() {
        this.cache.clear();
    }
}