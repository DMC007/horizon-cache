package org.horizon.caffeine;

import org.horizon.cache.Cache;
import org.horizon.enums.CacheTypeEnum;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author zhaoxun
 * @date 2025/12/4
 */
public class CacheManager {

    private volatile ConcurrentHashMap<String, Cache> cacheMap = new ConcurrentHashMap<>();
    private volatile CacheTypeEnum cacheTypeEnum = CacheTypeEnum.CAFFEINE;
    //默认最大本地缓存数
    private int maxSize = 10000;
    //默认缓存过期时间间隔秒数(单位:秒)
    private long expireAfterWrite = 60 * 10;

    public CacheManager(CacheTypeEnum cacheTypeEnum, int maxSize, long expireAfterWrite) {
        if (cacheTypeEnum != null) {
            this.cacheTypeEnum = cacheTypeEnum;
        }
        if (maxSize > 0) {
            this.maxSize = maxSize;
        }
        if (expireAfterWrite > 0) {
            this.expireAfterWrite = expireAfterWrite;
        }
    }

    /**
     * 启动缓存服务
     */
    public void start() {
    }

    /**
     * 停止缓存服务
     */
    public void stop() {
    }

    /**
     * 根据类别名称获取缓存操作对象实例，如果不存在就创建
     *
     * @param categoryName 类别名称
     * @return 缓存实例
     */
    public Cache getCache(String categoryName) {
        Cache cache = cacheMap.get(categoryName);
        if (cache == null) {
            synchronized (this) {
                cache = cacheMap.get(categoryName);
                if (cache == null) {
                    try {
                        cache = createCache(cacheTypeEnum);
                        cacheMap.put(categoryName, cache);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create cache for category: " + categoryName);
                    }
                }
            }
        }
        return cache;
    }


    /**
     * 创建本地缓存操作对象
     *
     * @param cacheTypeEnum 缓存组件类别
     * @return 缓存操作对象实例
     */
    private Cache createCache(CacheTypeEnum cacheTypeEnum) {
        if (cacheTypeEnum == CacheTypeEnum.CAFFEINE) {
            return new CaffeineCache(maxSize, expireAfterWrite, TimeUnit.SECONDS);
        } else {
            throw new RuntimeException("cache type not support!!!");
        }
    }
}
