package org.horizon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhaoxun
 * @date 2025/12/10
 * @descriprion 业务统一操作类封装，开发者只需调用该类方法即可实现1，2级缓存操作
 */
public class HorizonCacheHelper {
    private static final Logger log = LoggerFactory.getLogger(HorizonCacheHelper.class);

    private static final ConcurrentHashMap<String, HorizonCache> cacheMap = new ConcurrentHashMap<>();

    public static HorizonCache getCache(String categoryName, long survivalTime) {
        if (categoryName == null || categoryName.isEmpty()) {
            throw new RuntimeException("categoryName can not be null or empty");
        }

        return cacheMap.computeIfAbsent(categoryName, k -> {
            try {
                return new HorizonCache(categoryName, survivalTime);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create cache for category: " + categoryName);
            }
        });
    }

    public static HorizonCache getCache(String categoryName) {
        return getCache(categoryName, -1);
    }

    public static class HorizonCache {
        private String category;
        /**
         * 缓存存活时间，单位毫秒，-1表示永久存活
         */
        private long survivalTime;

        public HorizonCache(String category, long survivalTime) {
            this.category = category;
            this.survivalTime = survivalTime;
        }

        public HorizonCache(String category) {
            this.category = category;
            this.survivalTime = -1;
        }
    }
}
