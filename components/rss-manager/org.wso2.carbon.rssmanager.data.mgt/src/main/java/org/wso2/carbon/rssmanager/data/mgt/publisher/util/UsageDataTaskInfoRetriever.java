package org.wso2.carbon.rssmanager.data.mgt.publisher.util;

import java.util.Map;

import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskInfo.TriggerInfo;
import org.wso2.carbon.rssmanager.data.mgt.publisher.impl.RSSScheduleDataPublisher;

public class UsageDataTaskInfoRetriever {
	
	public static TaskInfo getTaskEnvironment(final String taskName, final String cronExpression, final Map<String,String> props){
        TriggerInfo triggerInfo = new TriggerInfo();
        triggerInfo.setCronExpression(cronExpression);
        return new TaskInfo(taskName, RSSScheduleDataPublisher.class.getName(), props, triggerInfo);
    }

}
