package org.horizon.cache;

import org.horizon.caffeine.CacheValue;

/**
 * @author zhaoxun
 * @date 2025/12/4
 */
public interface Cache {

    void set(String key, CacheValue cacheValue);

    CacheValue get(String key);

    void delete(String key);

    Boolean exists(String key);
}
