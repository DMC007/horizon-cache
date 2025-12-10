package org.horizon.enums;

import org.horizon.serialize.Serializer;
import org.horizon.serialize.impl.JavaSerializer;

/**
 * @author zhaoxun
 * @date 2025/12/4
 */
public enum SerializerTypeEnum {
    JAVA("java", new JavaSerializer());

    private String type;
    private Serializer serializer;

    SerializerTypeEnum(String type, Serializer serializer) {
        this.type = type;
        this.serializer = serializer;
    }

    public String getType() {
        return type;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public static SerializerTypeEnum match(String name) {
        for (SerializerTypeEnum item : SerializerTypeEnum.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return null;
    }
}
