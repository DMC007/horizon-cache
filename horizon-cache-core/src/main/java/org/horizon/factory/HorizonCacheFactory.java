package org.horizon.factory;

import org.horizon.cache.CacheManager;
import org.horizon.enums.CacheTypeEnum;
import org.horizon.enums.SerializerTypeEnum;
import org.horizon.redis.RedisManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedisPubSub;

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

    //原子标识，用来标识当前缓存工厂是否已经停止
    public volatile boolean isStop = false;

    private CacheManager l1CacheManager;
    private RedisManager l2CacheManager;

    //订阅通道[简单理解为mq的topic主题]
    private final String channel = "horizon-cache-channel";
    private Thread broadcastListenerThread;
    private BinaryJedisPubSub jedisPubSub;

    /**
     * 启动缓存工厂
     */
    public void start() {
        isStop = false;

        //校验一级缓存组件名称，暂时只支持caffeine
        if (!CacheTypeEnum.CAFFEINE.getType().equals(l1CacheProvider)) {
            throw new RuntimeException("l1 cache only support caffeine cache now!!!");
        }

        l1CacheManager = new CacheManager(CacheTypeEnum.CAFFEINE, maxSize, expireAfterWrite);
        l1CacheManager.start();

        //校验二级缓存组件名称，暂时只支持redis
        if (!CacheTypeEnum.REDIS.getType().equals(l2CacheProvider)) {
            throw new RuntimeException("l2 cache only support redis cache now!!!");
        }

        l2CacheManager = new RedisManager(serializer, nodes, username, password);
        l2CacheManager.start();

        //开启订阅
        subscribe();
        log.info("horizon-cache factory start success!!!");
    }


    /**
     * 开始订阅
     */
    public void subscribe() {
        jedisPubSub = new BinaryJedisPubSub() {
            @Override
            public void onMessage(byte[] channel, byte[] message) {
                //TODO 处理消息
                super.onMessage(channel, message);
            }

            @Override
            public void onSubscribe(byte[] channel, int subscribedChannels) {
                super.onSubscribe(channel, subscribedChannels);
            }

            @Override
            public void onUnsubscribe(byte[] channel, int subscribedChannels) {
                super.onUnsubscribe(channel, subscribedChannels);
            }
        };

        //监听线程启动
        broadcastListenerThread = new Thread(() -> {
            while (!isStop) {
                //TODO 订阅监听
            }
        }, "broadcastListenerThread");
        broadcastListenerThread.setDaemon(true);
        broadcastListenerThread.start();
    }


    public CacheManager getL1CacheManager() {
        return l1CacheManager;
    }

    public RedisManager getL2CacheManager() {
        return l2CacheManager;
    }

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
