package org.wso2.carbon.rssmanager.data.mgt.publisher.metadata;

public class PublishEventData {
	
	private final Object[] metaDataArray;
	private final Object[] correlationDataArray;
    private final Object[] payloadDataArray;
    
	public PublishEventData(Object[] metaDataArray, Object[] correlationDataArray,
                            Object[] payloadDataArray) {
	    super();
	    this.metaDataArray = metaDataArray;
	    this.correlationDataArray = correlationDataArray;
	    this.payloadDataArray = payloadDataArray;
    }


	public Object[] getMetaDataArray() {
		return metaDataArray;
	}

	public Object[] getCorrelationDataArray() {
		return correlationDataArray;
	}

	public Object[] getPayloadDataArray() {
		return payloadDataArray;
	}
    
    

}
