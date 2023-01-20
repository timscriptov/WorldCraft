package com.solverlabs.worldcraft.srv.util;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ExpiringMap<K, V> implements Map<K, V> {
    private final long expirationTime;
    private final Map<K, ExpiringObject<V>> map = new ConcurrentHashMap<>();
    private final Map<K, V> originalMap = new ConcurrentHashMap<>();

    public static class ExpiringObject<K> {
        private long lastModificationTime = System.currentTimeMillis();
        private final K object;
        private final long objectTtl;

        public ExpiringObject(K k, long j) {
            this.object = k;
            this.objectTtl = j;
        }

        public boolean equals(Object obj) {
            if (obj != null && (obj instanceof ExpiringObject)) {
                ExpiringObject expiringObject = (ExpiringObject) obj;
                if (expiringObject.getObject() != null) {
                    return expiringObject.getObject().equals(this.object);
                }
                return false;
            }
            return false;
        }

        public K getObject() {
            return this.object;
        }

        public int hashCode() {
            return this.object.hashCode();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - this.lastModificationTime > this.objectTtl;
        }

        public void updateLasModificationTime() {
            this.lastModificationTime = System.currentTimeMillis();
        }
    }

    public ExpiringMap(long j) {
        this.expirationTime = j;
    }

    @Override 
    public synchronized void clear() {
        this.originalMap.clear();
        this.map.clear();
    }

    @Override 
    public boolean containsKey(Object obj) {
        return this.map.containsKey(obj);
    }

    @Override 
    public boolean containsValue(Object obj) {
        return this.originalMap.containsValue(obj);
    }

    @NonNull
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return this.originalMap.entrySet();
    }

    @Override 
    public V get(Object obj) {
        ExpiringObject<V> expiringObject = this.map.get(obj);
        if (expiringObject == null) {
            return null;
        }
        return expiringObject.getObject();
    }

    @Override 
    public boolean isEmpty() {
        return this.map.size() <= 0;
    }

    @Override 
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Override 
    public synchronized V put(K k, V v) {
        if (k == null) {
            v = null;
        } else {
            ExpiringObject<V> expiringObject = this.map.get(k);
            if (expiringObject == null) {
                expiringObject = new ExpiringObject<>(v, this.expirationTime);
            }
            expiringObject.updateLasModificationTime();
            this.originalMap.put(k, v);
            this.map.put(k, expiringObject);
        }
        return v;
    }

    @Override 
    public void putAll(@NonNull Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override 
    public synchronized V remove(Object obj) {
        ExpiringObject<V> remove;
        this.originalMap.remove(obj);
        remove = this.map.remove(obj);
        return remove == null ? null : remove.getObject();
    }

    public synchronized void removeExpiredObjects() {
        Iterator<Map.Entry<K, ExpiringObject<V>>> it = this.map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<K, ExpiringObject<V>> next = it.next();
            if (next != null && next.getValue() != null && next.getValue().isExpired()) {
                it.remove();
            }
        }
    }

    @Override 
    public int size() {
        return this.map.size();
    }

    @NonNull
    @Override
    public Collection<V> values() {
        return this.originalMap.values();
    }
}
