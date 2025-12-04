package org.horizon.serialize;

/**
 * @author zhaoxun
 * @date 2025/12/4
 */
public abstract class Serializer {

    /**
     * 序列化
     */
    public abstract <T> byte[] serialize(T obj);

    /**
     * 反序列化
     */
    public abstract <T> T deserialize(byte[] bytes);
}
