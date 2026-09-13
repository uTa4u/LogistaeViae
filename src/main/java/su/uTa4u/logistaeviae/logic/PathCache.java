package su.uTa4u.logistaeviae.logic;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.*;

public class PathCache {
    private final Long2ObjectMap<List<Edge>> cache = new Long2ObjectOpenHashMap<>();

    public List<Edge> get(long key) {
        return this.cache.get(key);
    }

    public void put(long key, List<Edge> path) {
        this.cache.put(key, Collections.unmodifiableList(new ArrayList<>(path)));
    }

    public void clear() {
        this.cache.clear();
    }
}