/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cassandra.search.utils;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Serializer;

import java.util.ArrayList;
import java.util.List;

public class CassandraSerializer {
    private Serializer serializer = null;
    private boolean isCompositeSerializer = false;
    private List<Serializer> compositeSerializerList = null;

    public CassandraSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }

    public boolean isCompositeSerializer() {
        return isCompositeSerializer;
    }

    private void setCompositeSerializer(boolean compositeSerializer) {
        isCompositeSerializer = compositeSerializer;
    }

    public List<Serializer> getCompositeSerializerList() {
        return compositeSerializerList;
    }

    public void setCompositeSerializerList(String comparatorStr) {
        this.compositeSerializerList = new ArrayList<Serializer>();
        setCompositeSerializer(true);
        String serializeStr     = comparatorStr.substring(comparatorStr.indexOf('(') + 1,comparatorStr.lastIndexOf(')'));
        String[] serializeStrArr= serializeStr.split(",");

        for(String className : serializeStrArr) {
            Serializer tempSerializer = CassandraUtils.getSerializer(className);
            compositeSerializerList.add(tempSerializer != null ? tempSerializer : new StringSerializer());
        }
    }
}
