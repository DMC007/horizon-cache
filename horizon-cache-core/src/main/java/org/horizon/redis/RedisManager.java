package org.horizon.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.horizon.cache.Cache;
import org.horizon.enums.SerializerTypeEnum;
import org.horizon.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zhaoxun
 * @date 2025/12/4
 */
public class RedisManager {
    private static Logger log = LoggerFactory.getLogger(RedisManager.class);

    private String serializerType;
    private String nodes;
    private String username;
    private String password;

    private Serializer serializer = SerializerTypeEnum.JAVA.getSerializer();
    //jedis集群节点
    private Set<HostAndPort> clusterNodes = new HashSet<>();
    //连接超时时间
    private int connectionTime = 2000;
    //socket超时
    private int socketTimeout = 2000;
    private int maxAttempts = 3;

    public RedisManager(String serializerType, String nodes, String username, String password) {
        this.serializerType = serializerType;
        this.nodes = nodes;
        if (username != null && !username.trim().isEmpty()) {
            this.username = username;
        }
        if (password != null && !password.trim().isEmpty()) {
            this.password = password;
        }

        SerializerTypeEnum serializerTypeEnum = SerializerTypeEnum.match(this.serializerType);
        if (serializerTypeEnum != null) {
            serializer = serializerTypeEnum.getSerializer();
        }
        if (nodes != null && !nodes.trim().isEmpty()) {
            for (String node : nodes.split(",")) {
                String[] nodeArr = node.split(":");
                if (nodeArr.length != 2) {
                    continue;
                }
                HostAndPort hostAndPort = HostAndPort.from(node);
                clusterNodes.add(hostAndPort);
            }
        }

    }

    private JedisPool jedisPool;
    private JedisCluster jedisCluster;
    private RedisCache defaultRedisCache;


    /**
     * 启动redis服务初始化扩展方法
     */
    public void start() {
        if (clusterNodes == null || clusterNodes.isEmpty()) {
            throw new IllegalArgumentException("clusterNodes can not be empty!");
        }

        //判断是集群还是单节点
        if (clusterNodes.size() > 1) {
            try {
                GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
                //客户端连接redis配置
                DefaultJedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder()
                        .user(username)
                        .password(password)
                        .connectionTimeoutMillis(connectionTime)
                        .socketTimeoutMillis(socketTimeout)
                        .build();
                jedisCluster = new JedisCluster(clusterNodes, jedisClientConfig, maxAttempts, poolConfig);
                log.info("RedisManager (JedisCluster) initialized successfully.");
            } catch (Exception e) {
                log.error("RedisManager (JedisCluster) initialization failed.", e);
                throw new RuntimeException("RedisManager (JedisCluster) initialization failed.");
            }
        } else {
            try {
                JedisPoolConfig poolConfig = new JedisPoolConfig();
                //客户端连接redis配置
                DefaultJedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder()
                        .user(username)
                        .password(password)
                        .connectionTimeoutMillis(connectionTime)
                        .socketTimeoutMillis(socketTimeout)
                        .build();
                jedisPool = new JedisPool(poolConfig, clusterNodes.iterator().next(), jedisClientConfig);
                log.info("RedisManager (JedisPool) initialized successfully.");
            } catch (Exception e) {
                log.error("RedisManager (JedisPool) initialization failed.", e);
                throw new RuntimeException("RedisManager (JedisPool) initialization failed.");
            }
        }
        defaultRedisCache = new RedisCache(jedisPool, jedisCluster, serializer);
    }

    /**
     * 停止redis服务初始化扩展方法
     */
    public void stop() {
        try {
            if (jedisPool != null) {
                jedisPool.close();
            }
            if (jedisCluster != null) {
                jedisCluster.close();
            }
            log.info("RedisManager stopped.");
        } catch (Exception e) {
            log.error("RedisManager stop error.", e);
        }
    }

    /**
     * 获取缓存操作对象实例
     *
     * @return 缓存操作对象实例
     */
    public Cache getCache() {
        return defaultRedisCache;
    }
}
