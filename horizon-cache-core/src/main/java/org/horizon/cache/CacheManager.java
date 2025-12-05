package org.horizon.cache;

import org.horizon.enums.CacheTypeEnum;

import java.util.concurrent.ConcurrentHashMap;

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
        this.cacheTypeEnum = cacheTypeEnum;
        this.maxSize = maxSize;
        this.expireAfterWrite = expireAfterWrite;
    }

    /**
     * 启动缓存服务扩展方法
     */
    public void start() {
    }

    /**
     * 停止缓存服务扩展方法
     */
    public void stop() {
    }
}
