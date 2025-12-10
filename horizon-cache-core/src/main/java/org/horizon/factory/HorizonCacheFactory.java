package org.horizon.factory;

import org.horizon.broadcast.CacheBroadcastMessage;
import org.horizon.caffeine.CacheManager;
import org.horizon.caffeine.CacheValue;
import org.horizon.enums.CacheTypeEnum;
import org.horizon.enums.SerializerTypeEnum;
import org.horizon.redis.RedisCache;
import org.horizon.redis.RedisManager;
import org.horizon.utils.CacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedisPubSub;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

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

    public void stop() {
        isStop = true;
        if (l1CacheManager != null) {
            l1CacheManager.stop();
        }
        if (l2CacheManager != null) {
            l2CacheManager.stop();
        }
        if (jedisPubSub != null) {
            //解除订阅后，触发2阶段提交，若广播线程下次执行while则退出循环并结束
            jedisPubSub.unsubscribe();
        }
        if (broadcastListenerThread != null) {
            try {
                if (broadcastListenerThread.getState() != Thread.State.TERMINATED) {
                    broadcastListenerThread.interrupt();
                    try {
                        broadcastListenerThread.join();
                    } catch (Exception e) {
                        log.error("broadcast listener thread join error:{}", e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                log.error("broadcast listener thread stop error:{}", e.getMessage(), e);
            }
        }

        log.info("horizon-cache factory stop finish!!!");
    }


    /**
     * 开始订阅
     */
    public void subscribe() {
        jedisPubSub = new BinaryJedisPubSub() {
            @Override
            public void onMessage(byte[] channel, byte[] message) {
                //解码消息
                CacheBroadcastMessage broadcastMessage = SerializerTypeEnum.JAVA.getSerializer().deserialize(message);
                //拿到通知的最终key
                String finalKey = CacheUtil.generateKey(broadcastMessage.getCategory(), broadcastMessage.getKey());
                //获取L2缓存值，用于更新L1缓存
                CacheValue cacheValue = HorizonCacheFactory.getInstance().getL2CacheManager().getCache().get(finalKey);
                if (cacheValue == null) {
                    cacheValue = new CacheValue(null);
                }
                //更新L1缓存
                HorizonCacheFactory.getInstance().getL1CacheManager().getCache(broadcastMessage.getCategory()).set(finalKey, cacheValue);
                log.info("horizon-cache factory receive broadcast message, key: {}, value: {}", finalKey, cacheValue);
            }

            @Override
            public void onSubscribe(byte[] channel, int subscribedChannels) {
                log.info("horizon-cache factory subscribe channel: {}, total channels:{}", new String(channel, StandardCharsets.UTF_8), subscribedChannels);
            }

            @Override
            public void onUnsubscribe(byte[] channel, int subscribedChannels) {
                log.info("horizon-cache factory unsubscribe channel: {}, total channels:{}", new String(channel, StandardCharsets.UTF_8), subscribedChannels);
            }
        };

        //监听线程启动
        broadcastListenerThread = new Thread(() -> {
            while (!isStop) {
                //开启订阅监听线程
                while (!isStop) {
                    try {
                        RedisCache redisCache = (RedisCache) l2CacheManager.getCache();
                        //这个方法会一直阻塞当前线程，直到发生异常或手动取消订阅
                        redisCache.subscribe(channel, jedisPubSub);
                    } catch (Exception e) {
                        log.error("broadcast listener thread error", e);
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e1) {
                            log.error("broadcast listener [catch] thread error", e1);
                        }
                    }
                }
            }
        }, "broadcastListenerThread");
        broadcastListenerThread.setDaemon(true);
        broadcastListenerThread.start();
    }

    /**
     * 广播消息
     *
     * @param message 广播消息
     */
    public void broadcast(CacheBroadcastMessage message) {
        RedisCache redisCache = (RedisCache) l2CacheManager.getCache();
        redisCache.publish(channel, message);
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
