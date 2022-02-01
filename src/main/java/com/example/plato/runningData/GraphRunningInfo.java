package com.example.plato.runningData;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/30 14:56
 */
@Getter
public class GraphRunningInfo {

    /**
     * <uniqueId,NodeRunningInfo>
     */
    private Map<String, NodeRunningInfo> nodeRunningInfoMap = new ConcurrentHashMap<>();

    public GraphRunningInfo() {
    }

    public NodeRunningInfo getNodeRunningInfo(String uniqueId) {
        if (StringUtils.isBlank(uniqueId) || !nodeRunningInfoMap.containsKey(uniqueId)) {
            return null;
        }
        return nodeRunningInfoMap.get(uniqueId);
    }
}
