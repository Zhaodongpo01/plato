package com.example.plato.handler;

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
public class PreHandlerParser<P> extends AbstractHandler implements PreHandler<P> {

    public PreHandlerParser(NodeConfig nodeConfig) {
        super(nodeConfig);
    }

    @Override
    public P paramHandle(GraphRunningInfo graphRunningInfo) {
        checkNodeConfig();
        NodeConfig nodeConfig = getNodeConfig();
        String preHandler = nodeConfig.getPreHandler();
        if (StringUtils.isBlank(preHandler)) {
            return null;
        }
        return ((PreHandler<P>) getHandler(preHandler)).paramHandle(graphRunningInfo);
    }

    @Override
    public boolean suicide(GraphRunningInfo graphRunningInfo) {
        checkNodeConfig();
        NodeConfig nodeConfig = getNodeConfig();
        String preHandler = nodeConfig.getPreHandler();
        if (StringUtils.isBlank(preHandler)) {
            return PreHandler.super.suicide(graphRunningInfo);
        }
        PreHandler handler = (PreHandler) getHandler(preHandler);
        return handler.suicide(graphRunningInfo);
    }
}

