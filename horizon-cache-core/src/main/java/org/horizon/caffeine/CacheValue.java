package org.horizon.caffeine;

import java.io.Serializable;

public class CacheValue implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Long NOW_EXPIRATION_PERIOD = 1000L * 60 * 60 * 24 * 365 * 99;

    /**
     * 业务缓存对象
     */
    private Object value;

    /**
     * 缓存对象存活时间，单位毫秒
     */
    private long survivalTime;

    /**
     * 缓存对象失效时间，单位毫秒【表示真实过期时间的时间戳】
     */
    private long expirationTime;

    public CacheValue() {
    }

    public CacheValue(Object value) {
        this.value = value;
        this.survivalTime = NOW_EXPIRATION_PERIOD;
        this.expirationTime = System.currentTimeMillis() + NOW_EXPIRATION_PERIOD;
    }

    public CacheValue(Object value, long survivalTime) {
        this.value = value;
        this.survivalTime = survivalTime;
        this.expirationTime = System.currentTimeMillis() + survivalTime;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public long getSurvivalTime() {
        return survivalTime;
    }

    public void setSurvivalTime(long survivalTime) {
        this.survivalTime = survivalTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    @Override
    public String toString() {
        return "CacheValue{" +
                "value=" + value +
                ", survivalTime=" + survivalTime +
                ", expirationTime=" + expirationTime +
                '}';
    }

    /**
     * 判断缓存对象是否过期
     *
     * @return true:未过期，false:已过期
     */
    public boolean isValid() {
        return System.currentTimeMillis() < expirationTime;
    }
}
