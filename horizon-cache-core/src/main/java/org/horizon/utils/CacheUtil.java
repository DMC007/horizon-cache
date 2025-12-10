package org.horizon.utils;

public class CacheUtil {

    /**
     * 生成缓存key
     *
     * @param category 业务类别
     * @param key      业务key
     * @return 缓存key
     */
    public static String generateKey(String category, String key) {
        if (category == null || category.trim().isEmpty()) {
            throw new RuntimeException("category is null");
        }
        if (key == null || key.trim().isEmpty()) {
            throw new RuntimeException("key is null");
        }

        return category + ":" + key;
    }
}
