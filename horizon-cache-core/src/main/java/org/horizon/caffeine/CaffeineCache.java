package org.horizon.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.horizon.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class CaffeineCache implements Cache {
    private static final Logger log = LoggerFactory.getLogger(CaffeineCache.class);

    private final com.github.benmanes.caffeine.cache.Cache<String, CacheValue> cache;

    public CaffeineCache(int maxSize, long expireAfterWrite, TimeUnit timeUnit) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireAfterWrite, timeUnit)
                .build();
    }

    @Override
    public void set(String key, CacheValue cacheValue) {
        cache.put(key, cacheValue);
    }

    @Override
    public CacheValue get(String key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void delete(String key) {
        cache.invalidate(key);
    }

    @Override
    public Boolean exists(String key) {
        CacheValue cacheValue = get(key);
        if (cacheValue != null) {
            return cacheValue.getValue() != null && cacheValue.isValid();
        }
        return null;
    }
}
