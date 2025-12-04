package org.horizon.serialize.impl;

import org.horizon.serialize.Serializer;

/**
 * @author zhaoxun
 * @date 2025/12/4
 * @descriprion java序列化和反序列化
 */
public class JavaSerializer extends Serializer {
    @Override
    public <T> byte[] serialize(T obj) {
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] bytes) {
        return null;
    }
}
