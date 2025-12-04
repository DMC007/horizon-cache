package org.horizon.factory;

import org.horizon.enums.CacheTypeEnum;
import org.horizon.enums.SerializerTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhaoxun
 * @date 2025/12/4
 * @descriprion 缓存工厂, 用于管理配置, 启动和停止2级缓存相关服务
 */
public class HorizonCacheFactory {
    private static final Logger log = LoggerFactory.getLogger(HorizonCacheFactory.class);

    private static HorizonCacheFactory horizonCacheFactory;

    public static HorizonCacheFactory getInstance() {
        if (horizonCacheFactory == null) {
            throw new RuntimeException("horizon-cache factory not init!!!");
        }
        return horizonCacheFactory;
    }

    public HorizonCacheFactory() {
        horizonCacheFactory = this;
    }

    // 一级缓存提供者：默认走caffeine
    private String l1CacheProvider = CacheTypeEnum.CAFFEINE.getType();
    private int maxSize;
    private long expireAfterWrite;

    // 二级缓存提供者：默认走redis
    private String l2CacheProvider = CacheTypeEnum.REDIS.getType();
    private String serializer = SerializerTypeEnum.JAVA.getType();
    private String nodes;
    private String username;
    private String password;

    public String getL1CacheProvider() {
        return l1CacheProvider;
    }

    public void setL1CacheProvider(String l1CacheProvider) {
        this.l1CacheProvider = l1CacheProvider;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public long getExpireAfterWrite() {
        return expireAfterWrite;
    }

    public void setExpireAfterWrite(long expireAfterWrite) {
        this.expireAfterWrite = expireAfterWrite;
    }

    public String getL2CacheProvider() {
        return l2CacheProvider;
    }

    public void setL2CacheProvider(String l2CacheProvider) {
        this.l2CacheProvider = l2CacheProvider;
    }

    public String getSerializer() {
        return serializer;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public String getNodes() {
        return nodes;
    }

    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
