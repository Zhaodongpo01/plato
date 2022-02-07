package com.example.plato.loader.ymlHandler;

import org.apache.commons.lang3.StringUtils;

import com.example.plato.handler.PreHandler;
import com.example.plato.loader.config.NodeConfig;
import com.example.plato.runningData.GraphRunningInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/7 3:10 下午
 */
@Slf4j
public class YmlPreHandler<R> extends AbstractYmlHandler implements PreHandler<R> {

    public YmlPreHandler(NodeConfig nodeConfig) {
        super(nodeConfig);
    }

    @Override
    public R paramHandle(GraphRunningInfo graphRunningInfo) {
        checkNodeConfig();
        NodeConfig nodeConfig = getNodeConfig();
        String preHandler = nodeConfig.getPreHandler();
        if (StringUtils.isBlank(preHandler)) {
            return null;
        }
        return ((PreHandler<R>) getHandler(preHandler)).paramHandle(graphRunningInfo);
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
