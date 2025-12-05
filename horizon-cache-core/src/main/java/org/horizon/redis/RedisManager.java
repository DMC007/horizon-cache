package org.horizon.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public RedisManager(String serializerType, String nodes, String username, String password) {
        this.serializerType = serializerType;
        this.nodes = nodes;
        this.username = username;
        this.password = password;
    }

    /**
     * 启动redis服务初始化扩展方法
     */
    public void start() {
    }

    /**
     * 停止redis服务初始化扩展方法
     */
    public void stop() {
    }
}
