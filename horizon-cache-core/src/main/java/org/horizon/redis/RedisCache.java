package org.horizon.redis;

import org.horizon.cache.Cache;
import org.horizon.caffeine.CacheValue;
import org.horizon.enums.SerializerTypeEnum;
import org.horizon.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.nio.charset.StandardCharsets;

public class RedisCache implements Cache {

    private static final Logger log = LoggerFactory.getLogger(RedisCache.class);

    private JedisPool jedisPool;
    private JedisCluster jedisCluster;
    private Serializer serializer;

    public RedisCache(JedisPool jedisPool, JedisCluster jedisCluster, Serializer serializer) {
        this.jedisPool = jedisPool;
        this.jedisCluster = jedisCluster;
        this.serializer = serializer;
    }

    @Override
    public void set(String key, CacheValue cacheValue) {
        if (jedisCluster != null) {
            try {
                byte[] bytes = serializer.serialize(cacheValue);
                if (cacheValue.getSurvivalTime() < 0) {
                    jedisCluster.set(key.getBytes(StandardCharsets.UTF_8), bytes);
                } else {
                    jedisCluster.psetex(key.getBytes(StandardCharsets.UTF_8), cacheValue.getSurvivalTime(), bytes);
                }
            } catch (Exception e) {
                log.error("RedisCache set error:{}", e.getMessage(), e);
            }
        } else {
            try (Jedis jedis = jedisPool.getResource()) {
                byte[] bytes = serializer.serialize(cacheValue);
                if (cacheValue.getSurvivalTime() < 0) {
                    jedis.set(key.getBytes(StandardCharsets.UTF_8), bytes);
                } else {
                    jedis.psetex(key.getBytes(StandardCharsets.UTF_8), cacheValue.getSurvivalTime(), bytes);
                }
            } catch (Exception e) {
                log.error("RedisCache set error:{}", e.getMessage(), e);
            }
        }
    }

    @Override
    public CacheValue get(String key) {
        if (jedisCluster != null) {
            try {
                byte[] bytes = jedisCluster.get(key.getBytes(StandardCharsets.UTF_8));
                if (bytes == null) {
                    return null;
                }
                return serializer.deserialize(bytes);
            } catch (Exception e) {
                log.error("RedisCache get error:{}", e.getMessage(), e);
            }
        } else {
            try (Jedis jedis = jedisPool.getResource()) {
                byte[] bytes = jedis.get(key.getBytes(StandardCharsets.UTF_8));
                if (bytes == null) {
                    return null;
                }
                return serializer.deserialize(bytes);
            } catch (Exception e) {
                log.error("RedisCache get error:{}", e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public void delete(String key) {
        if (jedisCluster != null) {
            try {
                jedisCluster.del(key.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.error("RedisCache delete error:{}", e.getMessage(), e);
            }
        } else {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.del(key.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.error("RedisCache delete error:{}", e.getMessage(), e);
            }
        }
    }

    @Override
    public Boolean exists(String key) {
        if (jedisCluster != null) {
            try {
                return jedisCluster.exists(key.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.error("RedisCache exists error:{}", e.getMessage(), e);
            }
        } else {
            try (Jedis jedis = jedisPool.getResource()) {
                return jedis.exists(key.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.error("RedisCache exists error:{}", e.getMessage(), e);
            }
        }
        return false;
    }

    //------核心-------


    public void publish(String channel, Object message) {
        byte[] bytes = SerializerTypeEnum.JAVA.getSerializer().serialize(message);
        if (jedisCluster != null) {
            try {
                jedisCluster.publish(channel.getBytes(StandardCharsets.UTF_8), bytes);
            } catch (Exception e) {
                log.error("RedisCache publish error:{}", e.getMessage(), e);
            }
        } else {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(channel.getBytes(StandardCharsets.UTF_8), bytes);
            } catch (Exception e) {
                log.error("RedisCache publish error:{}", e.getMessage(), e);
            }
        }
    }


    /**
     * 订阅
     *
     * @param channel     订阅的频道
     * @param jedisPubSub 订阅的回调
     */
    public void subscribe(String channel, BinaryJedisPubSub jedisPubSub) {
        if (jedisCluster != null) {
            try {
                jedisCluster.subscribe(jedisPubSub, channel.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.error("RedisCache subscribe error:{}", e.getMessage(), e);
            }
        } else {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(jedisPubSub, channel.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                log.error("RedisCache subscribe error:{}", e.getMessage(), e);
            }
        }
    }
}
