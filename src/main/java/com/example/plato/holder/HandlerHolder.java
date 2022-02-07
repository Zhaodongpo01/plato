package com.example.plato.holder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.example.plato.loader.config.NodeConfig;
import com.example.plato.loader.ymlHandler.YmlAfterHandler;
import com.example.plato.loader.ymlHandler.YmlPreHandler;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/7 3:54 下午
 */
public class HandlerHolder {

    /**
     * <graphId:uniqueId:ymlPreHandler>
     */
    private static final Map<String, Map<String, YmlPreHandler>> preHandlerMap = new HashMap<>();

    /**
     * <graphId:uniqueId:ymlAfterHandler>
     */
    private static final Map<String, Map<String, YmlAfterHandler>> afterHandlerMap = new HashMap<>();

    public synchronized static YmlPreHandler putYmlPreHandler(String graphId,
            YmlPreHandler ymlPreHandler) {
        if (Objects.isNull(ymlPreHandler)) {
            return null;
        }
        NodeConfig nodeConfig = ymlPreHandler.getNodeConfig();
        if (StringUtils.isBlank(nodeConfig.getPreHandler())) {
            return null;
        }
        String uniqueId = nodeConfig.getUniqueId();
        if (!preHandlerMap.containsKey(graphId)) {
            Map<String, YmlPreHandler> ymlPreHandlerMap = new HashMap<>();
            preHandlerMap.put(graphId, ymlPreHandlerMap);
            ymlPreHandlerMap.put(uniqueId, ymlPreHandler);
        }
        return preHandlerMap.get(graphId).put(uniqueId, ymlPreHandler);
    }

    public synchronized static YmlAfterHandler putYmlAfterHandler(String graphId, YmlAfterHandler ymlAfterHandler) {
        if (Objects.isNull(ymlAfterHandler)) {
            return null;
        }
        NodeConfig nodeConfig = ymlAfterHandler.getNodeConfig();
        if (StringUtils.isBlank(nodeConfig.getAfterHandler())) {
            return null;
        }
        String uniqueId = nodeConfig.getUniqueId();
        if (!afterHandlerMap.containsKey(graphId)) {
            Map<String, YmlAfterHandler> ymlAfterHandlerMap = new HashMap<>();
            afterHandlerMap.put(graphId, ymlAfterHandlerMap);
            ymlAfterHandlerMap.put(uniqueId, ymlAfterHandler);
        }
        return afterHandlerMap.get(graphId).put(uniqueId, ymlAfterHandler);
    }

    public static YmlAfterHandler getYmlAfterHandler(String graphId, String uniqueId) {
        return afterHandlerMap.containsKey(graphId) ? afterHandlerMap.get(graphId).get(uniqueId) : null;
    }

    public static YmlPreHandler getYmlPreHandler(String graphId, String uniqueId) {
        return preHandlerMap.containsKey(graphId) ? preHandlerMap.get(graphId).get(uniqueId) : null;
    }

    public static Map<String, Map<String, YmlPreHandler>> getPreHandlerMap() {
        return preHandlerMap;
    }

    public static Map<String, Map<String, YmlAfterHandler>> getAfterHandlerMap() {
        return afterHandlerMap;
    }
}
