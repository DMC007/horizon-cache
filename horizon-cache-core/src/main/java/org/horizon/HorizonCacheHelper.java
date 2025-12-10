package org.horizon;

import org.horizon.broadcast.CacheBroadcastMessage;
import org.horizon.caffeine.CacheValue;
import org.horizon.factory.HorizonCacheFactory;
import org.horizon.utils.CacheUtil;
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

    /**
     * 获取缓存对象
     *
     * @param categoryName 缓存分类名称
     * @param survivalTime 缓存存活时间，单位毫秒，-1表示永久存活
     * @return 缓存对象
     */
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

    /**
     * 获取缓存对象
     *
     * @param categoryName 缓存分类名称
     * @return 缓存对象
     */
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

        /**
         * 设置缓存
         *
         * @param key   缓存key
         * @param value 缓存value
         */
        public void set(String key, Object value) {
            String finalKey = CacheUtil.generateKey(category, key);
            CacheValue cacheValue = new CacheValue(value, survivalTime);
            //存入L1缓存
            HorizonCacheFactory.getInstance().getL1CacheManager().getCache(category).set(finalKey, cacheValue);
            log.debug("HorizonCache, set l1-cache, key:{}, value:{}", finalKey, cacheValue);
            //存入L2缓存
            HorizonCacheFactory.getInstance().getL2CacheManager().getCache().set(finalKey, cacheValue);
            log.debug("HorizonCache, set l2-cache, key:{}, value:{}", finalKey, cacheValue);
            //广播,通知其他服务去redis获取最新的值，并更新自己的本地L1缓存
            HorizonCacheFactory.getInstance().broadcast(new CacheBroadcastMessage(category, key));
        }

        /**
         * 获取缓存
         *
         * @param key 缓存key
         * @return 缓存value
         */
        public <T> T get(String key) {
            String finalKey = CacheUtil.generateKey(category, key);

            //先看本地L1缓存
            CacheValue l1CacheValue = HorizonCacheFactory.getInstance().getL1CacheManager().getCache(category).get(finalKey);
            if (l1CacheValue != null) {
                //先看下是否还有效
                if (l1CacheValue.isValid()) {
                    log.debug("HorizonCache, get l1-cache, key:{}, value:{}", finalKey, l1CacheValue);
                    return (T) l1CacheValue.getValue();
                }
                return null;
            }

            //再看远程redis的L2缓存
            CacheValue l2CacheValue = HorizonCacheFactory.getInstance().getL2CacheManager().getCache().get(finalKey);
            log.debug("HorizonCache, get l2-cache, key:{}, value:{}", finalKey, l2CacheValue);
            if (l2CacheValue != null) {
                //L2缓存在存入redis的时候已经设置了过期时间，所以这里查出值说明还没到过期时间，故不用像L1缓存一样判断是否还有效
                //返回值前先给L1缓存赋值
                HorizonCacheFactory.getInstance().getL1CacheManager().getCache(category).set(finalKey, l2CacheValue);
                log.debug("HorizonCache, lazy set l1-cache, key:{}, value:{}", finalKey, l2CacheValue);
                return (T) l2CacheValue.getValue();
            } else {
                HorizonCacheFactory.getInstance().getL1CacheManager().getCache(category).set(finalKey, new CacheValue(null));
                log.debug("HorizonCache, lazy set l2-cache, key:{}, value:{}", finalKey, null);
                return null;
            }
        }

        /**
         * 删除缓存
         *
         * @param key 缓存key
         */
        public void delete(String key) {
            String finalKey = CacheUtil.generateKey(category, key);

            //删除L1缓存
            HorizonCacheFactory.getInstance().getL1CacheManager().getCache(category).delete(finalKey);
            log.debug("HorizonCache, delete l1-cache, key:{}", finalKey);

            //删除L2缓存
            HorizonCacheFactory.getInstance().getL2CacheManager().getCache().delete(finalKey);
            log.debug("HorizonCache, delete l2-cache, key:{}", finalKey);

            //广播,通知其他服务更新自己的本地L1缓存
            HorizonCacheFactory.getInstance().broadcast(new CacheBroadcastMessage(category, key));
        }

        /**
         * 判断缓存是否存在
         *
         * @param key 缓存key
         * @return 缓存是否存在
         */
        public boolean exists(String key) {
            String finalKey = CacheUtil.generateKey(category, key);
            Boolean exists = HorizonCacheFactory.getInstance().getL1CacheManager().getCache(category).exists(finalKey);
            if (exists == null) {
                //缓存不存在，则尝试从L2缓存中获取并设置到L1缓存
                get(key);
            }
            //再次尝试从L1缓存中获取
            exists = HorizonCacheFactory.getInstance().getL1CacheManager().getCache(category).exists(finalKey);
            return exists;
        }
    }
}
