package by.radioegor146;

import java.util.HashMap;
import java.util.Map;

public class NodeCache<T> {

    private final String pointerPattern;
    private final Map<T, Integer> cache;

    public NodeCache(String pointerPattern) {
        this.pointerPattern = pointerPattern;
        cache = new HashMap<>();
    }

    public String getPointer(T key) {
        return String.format(pointerPattern, getId(key));
    }

    public int getId(T key) {
        if(!cache.containsKey(key)) {
            cache.put(key, cache.size());
        }
        return cache.get(key);
    }

    public int size() {
        return cache.size();
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public Map<T, Integer> getCache() {
        return cache;
    }

    public void clear() {
        cache.clear();
    }
}
