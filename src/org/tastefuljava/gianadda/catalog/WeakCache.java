package org.tastefuljava.gianadda.catalog;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WeakCache<K,T> {
    private static final Logger LOG
            = Logger.getLogger(WeakCache.class.getName());

    private final Map<K,Ref> map = new HashMap<>();
    private ReferenceQueue<T> refQueue = new ReferenceQueue<>();

    public void put(K key, T obj) {
        cleanup();
        map.put(key, new Ref(key, obj));
    }

    public T get(K key) {
        cleanup();
        Ref ref = map.get(key);
        return ref == null ? null : ref.get();
    }

    public T getOrPut(K key, T obj) {
        cleanup();
        Ref ref = map.get(key);
        T result = ref == null ? null : ref.get();
        if (result != null) {
            return result;
        }
        map.put(key, new Ref(key, obj));
        return obj;
    }

    public T remove(K key) {
        cleanup();
        Ref ref = map.remove(key);
        return ref == null ? null : ref.get();
    }

    public void clear() {
        map.clear();
        refQueue = new ReferenceQueue<>();
    }

    private void cleanup() {
        while (refQueue.poll() != null) {
            try {
                Ref ref = (Ref)refQueue.remove(1);
                map.remove(ref.key);
            } catch (IllegalArgumentException | InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    private class Ref extends WeakReference<T> {
        private final K key;

        public Ref(K key, T obj) {
            super(obj, refQueue);
            this.key = key;
        }
    }
}
