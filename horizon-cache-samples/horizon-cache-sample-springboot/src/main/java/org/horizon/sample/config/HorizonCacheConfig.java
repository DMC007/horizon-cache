package org.horizon.sample.config;

import org.horizon.factory.HorizonCacheFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: DMC007
 * @date: 2026-02-11 22:21
 * @description: 核心配置类，读取配置文件相关信息注入属性
 */
@Configuration
public class HorizonCacheConfig {

    @Value("${horizon.cache.l1.provider}")
    private String l1Provider;
    @Value("${horizon.cache.l1.maxSize}")
    private int maxSize;
    @Value("${horizon.cache.l1.expireAfterWrite}")
    private long expireAfterWrite;
    @Value("${horizon.cache.l2.provider}")
    private String l2Provider;
    @Value("${horizon.cache.l2.serializer}")
    private String serializer;
    @Value("${horizon.cache.l2.nodes}")
    private String nodes;
    @Value("${horizon.cache.l2.username}")
    private String user;
    @Value("${horizon.cache.l2.password}")
    private String password;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public HorizonCacheFactory horizonCacheFactory() {
        HorizonCacheFactory horizonCacheFactory = new HorizonCacheFactory();
        horizonCacheFactory.setL1CacheProvider(l1Provider);
        horizonCacheFactory.setMaxSize(maxSize);
        horizonCacheFactory.setExpireAfterWrite(expireAfterWrite);
        horizonCacheFactory.setL2CacheProvider(l2Provider);
        horizonCacheFactory.setSerializer(serializer);
        horizonCacheFactory.setNodes(nodes);
        horizonCacheFactory.setUsername(user);
        horizonCacheFactory.setPassword(password);
        return horizonCacheFactory;
    }
}
