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

import me.prettyprint.cassandra.serializers.*;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.Serializer;
import me.prettyprint.hector.api.beans.Composite;
import me.prettyprint.hector.api.ddl.ComparatorType;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.commons.io.FileUtils;
import org.wso2.carbon.cassandra.search.exception.CassandraSearchException;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public final class CassandraUtils {
    private static final String STREAMDEFN_XML = "streamdefn.xml";
    private static final String KEY_SPACE_NAME_ELEMENT = "keySpaceName";
    private static final String INDEX_KEY_SPACE_NAME_ELEMENT = "eventIndexKeySpaceName";

    public static final String BYTESTYPE  = ComparatorType.BYTESTYPE.getClassName();

    private static final StringSerializer STRING_SERIALIZER   = StringSerializer.get();
    private static final IntegerSerializer INTEGER_SERIALIZER = IntegerSerializer.get();
    private static final LongSerializer LONG_SERIALIZER       = LongSerializer.get();
    private static final BooleanSerializer BOOLEAN_SERIALIZER = BooleanSerializer.get();
    private static final FloatSerializer FLOAT_SERIALIZER     = FloatSerializer.get();
    private static final DoubleSerializer DOUBLE_SERIALIZER     = DoubleSerializer.get();

    private static final Map<String, Serializer> SERIALIZER_MAP =
            new HashMap<String, Serializer>();

    private static final Map<AttributeType, String> ATTRIBUTE_TYPE_COMPARATOR_TYPE_MAP =
            new HashMap<AttributeType, String>();

    private static String keySpaceName;
    private static String indexKeySpaceName;

    private static final Map<String, CFInfo> COLUMN_FAMILY_CACHE = new HashMap<String, CFInfo>();
    // TODO Make this a bounded LRU cache to handle large cf number scenarios

    static {

        SERIALIZER_MAP.put(ComparatorType.UTF8TYPE.getClassName(), new StringSerializer());
        SERIALIZER_MAP.put(ComparatorType.ASCIITYPE.getClassName(), new AsciiSerializer());
        SERIALIZER_MAP.put(ComparatorType.LONGTYPE.getClassName(), new LongSerializer());
        SERIALIZER_MAP.put(ComparatorType.BYTESTYPE.getClassName(), new ByteBufferSerializer());
        SERIALIZER_MAP.put(ComparatorType.INTEGERTYPE.getClassName(), new IntegerSerializer());
        SERIALIZER_MAP.put(ComparatorType.UUIDTYPE.getClassName(), new UUIDSerializer());
        SERIALIZER_MAP.put(ComparatorType.TIMEUUIDTYPE.getClassName(), new TimeUUIDSerializer());
        SERIALIZER_MAP.put("org.apache.cassandra.db.marshal.Int32Type", new IntegerSerializer());
        SERIALIZER_MAP.put("org.apache.cassandra.db.marshal.DoubleType", new DoubleSerializer());
        SERIALIZER_MAP.put("org.apache.cassandra.db.marshal.BooleanType", new BooleanSerializer());
        SERIALIZER_MAP.put("org.apache.cassandra.db.marshal.FloatType", new FloatSerializer());
        SERIALIZER_MAP.put(ComparatorType.COMPOSITETYPE.getClassName(), new CompositeSerializer());
        SERIALIZER_MAP.put(ComparatorType.DYNAMICCOMPOSITETYPE.getClassName(), new DynamicCompositeSerializer());

        ATTRIBUTE_TYPE_COMPARATOR_TYPE_MAP.put(AttributeType.BOOL, "org.apache.cassandra.db.marshal.BooleanType");
        ATTRIBUTE_TYPE_COMPARATOR_TYPE_MAP.put(AttributeType.INT, ComparatorType.LONGTYPE.getClassName());
        ATTRIBUTE_TYPE_COMPARATOR_TYPE_MAP.put(AttributeType.DOUBLE, "org.apache.cassandra.db.marshal.DoubleType");
        ATTRIBUTE_TYPE_COMPARATOR_TYPE_MAP.put(AttributeType.FLOAT, "org.apache.cassandra.db.marshal.DoubleType");
        ATTRIBUTE_TYPE_COMPARATOR_TYPE_MAP.put(AttributeType.LONG, ComparatorType.LONGTYPE.getClassName());
        ATTRIBUTE_TYPE_COMPARATOR_TYPE_MAP.put(AttributeType.STRING, ComparatorType.UTF8TYPE.getClassName());
    }

    private CassandraUtils() {
    }

    public static Object getOriginalValueFromColumnValue(ByteBuffer byteBuffer, AttributeType attributeType)
            throws IOException {
        switch (attributeType) {
            case BOOL: {
                return BOOLEAN_SERIALIZER.fromByteBuffer(byteBuffer);
            }
            case INT: {
                return INTEGER_SERIALIZER.fromByteBuffer(byteBuffer);
            }
            case DOUBLE: {
                return DOUBLE_SERIALIZER.fromByteBuffer(byteBuffer);
            }
            case FLOAT: {
                return FLOAT_SERIALIZER.fromByteBuffer(byteBuffer);
            }
            case LONG: {
                return LONG_SERIALIZER.fromByteBuffer(byteBuffer);
            }
            case STRING: {
                return STRING_SERIALIZER.fromByteBuffer(byteBuffer);
            }
        }
        return null;
    }

    public static Object getValue(String val, AttributeType attributeType) throws CassandraSearchException {
        try {
            switch (attributeType) {
                case BOOL: {
                    return Boolean.parseBoolean(val);
                }
                case FLOAT:
                case DOUBLE: {
                    return Double.parseDouble(val);
                }
                case INT:
                case LONG: {
                    return Long.parseLong(val);
                }
                case STRING: {
                    return val;
                }
            }
        } catch (NumberFormatException e) {
            throw new CassandraSearchException(e.getMessage(), e);
        }
        return "";
    }

    public static String getMaxValueString(AttributeType attributeType) {
        Object val = null;
        switch (attributeType) {
            case FLOAT:
            case DOUBLE: {
                val = Double.MAX_VALUE;
                return val.toString();
            }
            case INT:
            case LONG: {
                val = Long.MAX_VALUE;
                return val.toString();
            }
        }
        return "";
    }

    public static String getMinValueString(AttributeType attributeType) {
        Object val = null;
        switch (attributeType) {
            case FLOAT:
            case DOUBLE: {
//                val = -Double.MAX_VALUE; //todo bug in hector. cannot perform range on double. check fo alternatives
                return "0";
            }
            case INT:
            case LONG: {
                val = Long.MIN_VALUE;
                return val.toString();
            }
        }
        return "";
    }

    public static String convertStreamNameToCFName(String streamName) {
        if (streamName == null) {
            return null;
        }
        return streamName.replace(":", "_").replace(".", "_");
    }

    public static String getCustomIndexCFName(String primaryCFName, String indexColumnName) {
        return String.valueOf(Math.abs((primaryCFName + indexColumnName).hashCode()));
    }

    public static String getComparator(AttributeType attributeType) {
        return ATTRIBUTE_TYPE_COMPARATOR_TYPE_MAP.get(attributeType);
    }

    public static Serializer getSerializer(String comparatorClass) {
        return SERIALIZER_MAP.get(comparatorClass);
    }

    public static CFInfo getColumnFamilyInfo(Cluster cluster, Keyspace keyspace,
                                             String columnFamilyName) {
        CFInfo cfInfo = COLUMN_FAMILY_CACHE.get(keyspace + "-" + columnFamilyName);

        if (cfInfo == null) {
            cfInfo = new CFInfo(cluster, keyspace, columnFamilyName);
            COLUMN_FAMILY_CACHE.put(keyspace + "-" + columnFamilyName, cfInfo);
        }

        return cfInfo;
    }

    public static String getStringDeserialization(Serializer serializer, ByteBuffer data) {
        if(serializer instanceof ByteBufferSerializer){
            serializer = new StringSerializer();
        }
        Object columnName = serializer.fromByteBuffer(data);
        return columnName.toString();
    }

    public static String getStringDeserialization(CassandraSerializer cassandraSerializer, ByteBuffer data) {
        Serializer serializer = cassandraSerializer.getSerializer();
        if(serializer instanceof ByteBufferSerializer){
            serializer = new StringSerializer();
        }
        Object columnName = serializer.fromByteBuffer(data);

        if(columnName == null) {
            return "";
        }

        if(columnName instanceof Composite && cassandraSerializer.getCompositeSerializerList() != null) {
            boolean isAdded = false;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");
            Composite composite = (Composite) columnName;
            for(int i = 0; i < cassandraSerializer.getCompositeSerializerList().size(); i++) {
                if(isAdded) {
                    stringBuilder.append(", ");
                }
                Object compositeColName = composite.get(i, cassandraSerializer.getCompositeSerializerList().get(i));
                stringBuilder.append(compositeColName.toString());
                isAdded = true;
            }
            stringBuilder.append("]");
            return stringBuilder.toString();
        }

        return columnName.toString();
    }

    public static void readConfigFile() {
        InputStream in;
        try {
            String configFilePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "advanced" + File.separator + STREAMDEFN_XML;
            in = FileUtils.openInputStream(new File(configFilePath));
        } catch (Exception e) {
            in = CassandraUtils.class.getClassLoader().getResourceAsStream(STREAMDEFN_XML);
        }

        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(in);

        OMElement documentElement = builder.getDocumentElement();

        OMElement keySpaceElement = documentElement.getFirstChildWithName(new QName(KEY_SPACE_NAME_ELEMENT));
        if (keySpaceElement != null){
            keySpaceName = keySpaceElement.getText();
        }else {
            keySpaceName = SearchConstants.DEFAULT_KEY_SPACE_NAME;
        }

        OMElement indexKeySpaceElement = documentElement.getFirstChildWithName(new QName(INDEX_KEY_SPACE_NAME_ELEMENT));
        if (indexKeySpaceElement != null){
            indexKeySpaceName = indexKeySpaceElement.getText();
        }else {
            indexKeySpaceName = SearchConstants.DEFAULT_INDEX_KEYSPACE_NAME;
        }
    }

    public static String getKeySpaceName() {
        return keySpaceName != null ? keySpaceName : SearchConstants.DEFAULT_KEY_SPACE_NAME;
    }

    public static String getIndexKeySpaceName() {
        return indexKeySpaceName != null ? indexKeySpaceName : SearchConstants.DEFAULT_INDEX_KEYSPACE_NAME;
    }

}

