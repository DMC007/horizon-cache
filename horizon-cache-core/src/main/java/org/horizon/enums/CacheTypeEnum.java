package org.horizon.enums;

/**
 * @author zhaoxun
 * @date 2025/12/4
 */
public enum CacheTypeEnum {
    CAFFEINE("caffeine"),
    REDIS("redis"),
    ;

    private String type;

    CacheTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static CacheTypeEnum match(String type) {
        for (CacheTypeEnum cacheTypeEnum : CacheTypeEnum.values()) {
            if (cacheTypeEnum.getType().equals(type)) {
                return cacheTypeEnum;
            }
        }
        return null;
    }
}
