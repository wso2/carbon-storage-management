package org.wso2.carbon.cassandra.common.cache;


import java.io.Serializable;

public class UserAccessKeyCacheEntry implements Serializable {

    private String accessKey;

    public UserAccessKeyCacheEntry(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getAccessKey() {
        return accessKey;
    }
}
