package com.example.plato.holder;

import com.example.plato.element.NodeLoadByBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 10:59 上午
 * 存放node运行时结果
 */
@Slf4j
public class NodeHolder {

    /**
     * <graphId:<uniqueId,NodeLoadByBean>>
     */
    private static Map<String, Map<String, NodeLoadByBean>> nodeMap = new ConcurrentHashMap<>();

    public static NodeLoadByBean getNodeLoadByBean(String graphId, String uniqueId) {
        if (StringUtils.isAnyBlank(graphId, uniqueId) || !nodeMap.containsKey(graphId)) {
            return null;
        }
        return nodeMap.get(uniqueId).get(uniqueId);
    }

    public static NodeLoadByBean putNodeLoadByBean(String graphId, String uniqueId, NodeLoadByBean nodeLoadByBean) {
        if (StringUtils.isAnyBlank(graphId, uniqueId) || !Optional.ofNullable(nodeLoadByBean).isPresent()) {
            return null;
        }
        if (nodeMap.containsKey(graphId)) {
            return nodeMap.get(graphId).put(uniqueId, nodeLoadByBean);
        }
        ConcurrentHashMap<String, NodeLoadByBean> concurrentHashMap = new ConcurrentHashMap<>();
        concurrentHashMap.put(uniqueId, nodeLoadByBean);
        nodeMap.put(uniqueId, concurrentHashMap);
        return nodeLoadByBean;
    }
}
