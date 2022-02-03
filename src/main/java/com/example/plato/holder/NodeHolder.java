package com.example.plato.holder;

import com.example.plato.element.AbstractNodeDefine;

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
    private static Map<String, Map<String, AbstractNodeDefine>> nodeMap = new ConcurrentHashMap<>();

    public static AbstractNodeDefine getNode(String graphId, String uniqueId) {
        if (StringUtils.isAnyBlank(graphId, uniqueId) || !nodeMap.containsKey(graphId)) {
            return null;
        }
        return nodeMap.get(uniqueId).get(uniqueId);
    }

    public static AbstractNodeDefine putNode(String graphId, String uniqueId,
            AbstractNodeDefine abstractNodeDefine) {
        if (StringUtils.isAnyBlank(graphId, uniqueId) || !Optional.ofNullable(abstractNodeDefine).isPresent()) {
            return null;
        }
        if (nodeMap.containsKey(graphId)) {
            return nodeMap.get(graphId).put(uniqueId, abstractNodeDefine);
        }
        ConcurrentHashMap<String, AbstractNodeDefine> concurrentHashMap = new ConcurrentHashMap<>();
        concurrentHashMap.put(uniqueId, abstractNodeDefine);
        nodeMap.put(uniqueId, concurrentHashMap);
        return abstractNodeDefine;
    }
}
