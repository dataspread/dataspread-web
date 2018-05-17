package org.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class LruCache<K, V> {
    Cache<K, V> cache;

    // Get the size = maxEntrs, when size >  maxEntries + evictSize
    public LruCache(int maxEntries) {
        if (maxEntries>0)
            cache = CacheBuilder.newBuilder()
                    .maximumSize(maxEntries)
                    .build();
        else
            cache = CacheBuilder.newBuilder()
                    .build();
    }

    public boolean containsKey(K key) {
        return cache.asMap().containsKey(key);
    }

    public void put(K key, V value) {
        if (value==null)
            return;
        cache.put(key, value);
        // Do not store null values.
    }

    public V get(K key) {
        return cache.getIfPresent(key);
    }

    public void remove(K key) {
        cache.invalidate(key);
    }

    public void clear() {
        cache.invalidateAll();
    }

    public Collection<K> keySet() {
        return cache.asMap().keySet();
    }
}
