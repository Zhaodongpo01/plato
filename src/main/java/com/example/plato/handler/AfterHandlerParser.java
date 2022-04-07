package com.example.plato.handler;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.example.plato.loader.loaderConfig.NodeConfig;
import com.example.plato.runningData.GraphRunningInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/20 12:18 下午
 */
@Slf4j
public class AfterHandlerParser extends AbstractHandler implements AfterHandler {

    public AfterHandlerParser(NodeConfig nodeConfig) {
        super(nodeConfig);
    }

    @Override
    public Set<String> notShouldRunNodes(GraphRunningInfo graphRunningInfo) {
        checkNodeConfig();
        NodeConfig nodeConfig = getNodeConfig();
        String afterHandler = nodeConfig.getAfterHandler();
        if (StringUtils.isBlank(afterHandler)) {
            return AfterHandler.super.notShouldRunNodes(graphRunningInfo);
        }
        AfterHandler handler = (AfterHandler) getHandler(afterHandler);
        return handler.notShouldRunNodes(graphRunningInfo);
    }

}
