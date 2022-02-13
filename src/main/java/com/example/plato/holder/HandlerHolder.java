package com.example.plato.holder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.plato.loader.config.NodeConfig;
import com.example.plato.loader.ymlHandler.YmlAfterHandler;
import com.example.plato.loader.ymlHandler.YmlPreHandler;
import com.example.plato.util.PlatoAssert;

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
        PlatoAssert.nullException(() -> "putYmlPreHandler ymlPreHandler is null", ymlPreHandler);
        NodeConfig nodeConfig = ymlPreHandler.getNodeConfig();
        PlatoAssert.emptyException(() -> "putYmlPreHandler preHandler error", nodeConfig.getPreHandler());
        String uniqueId = nodeConfig.getUniqueId();
        if (!preHandlerMap.containsKey(graphId)) {
            Map<String, YmlPreHandler> ymlPreHandlerMap = new ConcurrentHashMap<>();
            preHandlerMap.put(graphId, ymlPreHandlerMap);
            ymlPreHandlerMap.put(uniqueId, ymlPreHandler);
        }
        return preHandlerMap.get(graphId).put(uniqueId, ymlPreHandler);
    }

    public synchronized static YmlAfterHandler putYmlAfterHandler(String graphId, YmlAfterHandler ymlAfterHandler) {
        PlatoAssert.nullException(() -> "putYmlAfterHandler ymlAfterHandler is null", ymlAfterHandler);
        NodeConfig nodeConfig = ymlAfterHandler.getNodeConfig();
        PlatoAssert.emptyException(() -> "putYmlAfterHandler afterHandler error", nodeConfig.getAfterHandler());
        String uniqueId = nodeConfig.getUniqueId();
        if (!afterHandlerMap.containsKey(graphId)) {
            Map<String, YmlAfterHandler> ymlAfterHandlerMap = new ConcurrentHashMap<>();
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

}
