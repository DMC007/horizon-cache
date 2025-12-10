package org.horizon.broadcast;

import java.io.Serializable;

/**
 * @author DMC007
 * @date 2025/12/10
 * @descriprion redis广播消息，不含业务消息体，只包含业务key
 */
public class CacheBroadcastMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String category;
    private String key;

    public CacheBroadcastMessage() {
    }

    public CacheBroadcastMessage(String category, String key) {
        this.category = category;
        this.key = key;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
