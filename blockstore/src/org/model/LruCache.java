package org.model;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class LruCache<K, V> {
    private ConcurrentLinkedQueue<K> concurrentLinkedQueue;
    private ConcurrentHashMap<K,V> concurrentHashMap;
    
    private final int maxEntries;
    private final int evictSize;

    // Get the size = maxEntrs, when size >  maxEntries + evictSize
    public LruCache(int maxEntries, int evictSize) {
        concurrentHashMap = new ConcurrentHashMap<>();
        concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
        this.maxEntries = maxEntries;
        this.evictSize = evictSize;
    }

    public boolean containsKey(K key) {
        return concurrentHashMap.containsKey(key);
    }

    public void put(K key, V value) {
        // Do not store null values.
        if (value==null)
            return;
        V putVal = concurrentHashMap.put(key,value);
        if (putVal==null)
            concurrentLinkedQueue.add(key);

        if (maxEntries > -1 && concurrentLinkedQueue.size() > maxEntries+evictSize) {
            int itemsToRemove = concurrentLinkedQueue.size() - maxEntries;
            for (int i = 0; i < itemsToRemove; i++)
                concurrentHashMap.remove(concurrentLinkedQueue.remove());
        }
    }

    public V get(K key) {
        return concurrentHashMap.get(key);
    }

    public void remove(K key) {
        // Do not remove from queue concurrentLinkedQueue, as it is expensive.
        concurrentHashMap.remove(key);
    }

    public void clear() {
        concurrentLinkedQueue.clear();
        concurrentHashMap.clear();
    }

    public List<K> keySet() {
        return concurrentHashMap.keySet().stream().collect(Collectors.toList());
    }
}
