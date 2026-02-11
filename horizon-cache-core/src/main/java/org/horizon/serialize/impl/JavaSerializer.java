package org.horizon.serialize.impl;

import org.horizon.serialize.Serializer;

import java.io.*;

/**
 * @author zhaoxun
 * @date 2025/12/4
 * @descriprion java序列化和反序列化
 */
public class JavaSerializer extends Serializer {
    @Override
    public <T> byte[] serialize(T obj) {
        if (obj == null) {
            throw new RuntimeException("Cannot serialize null object");
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize object: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes) {
        if (bytes == null) {
            throw new RuntimeException("Cannot deserialize null byte array");
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (T) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize object: " + e.getMessage(), e);
        }
    }
}
