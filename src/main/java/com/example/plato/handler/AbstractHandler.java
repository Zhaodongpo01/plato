package com.example.plato.handler;

import org.apache.commons.lang3.StringUtils;

import com.example.plato.loader.loaderConfig.NodeConfig;
import com.example.plato.util.ClassUtil;
import com.example.plato.util.PlatoAssert;
import com.example.plato.util.SpringUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/20 12:18 下午
 */
@Data
@Slf4j
public abstract class AbstractHandler {

    private NodeConfig nodeConfig;

    public AbstractHandler(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    static final private String SPILIT = ":";

    public void checkNodeConfig() {
        PlatoAssert.nullException(() -> "AbstractHandler#nodeConfig must not null", nodeConfig);
    }

    public IHandler getHandler(String var) {
        if (StringUtils.isBlank(var)) {
            return null;
        }
        if (var.contains(SPILIT)) {
            String[] split = var.split(SPILIT);
            Object bean = SpringUtils.getBean(split[0]);
            String serviceName = bean.getClass().getName();
            return (IHandler) ClassUtil.methodInvoke(serviceName, split[1]);
        }
        return SpringUtils.getBean(var);
    }

}
