package com.example.plato.loader.ymlHandler;

import java.util.Objects;

import com.example.plato.exception.PlatoException;
import com.example.plato.loader.config.NodeConfig;
import com.example.plato.util.ClassUtil;
import com.example.plato.util.SpringUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/7 3:37 下午
 */
@Data
@Slf4j
public abstract class AbstractYmlHandler {

    private NodeConfig nodeConfig;

    public AbstractYmlHandler(NodeConfig nodeConfig) {
        this.nodeConfig = nodeConfig;
    }

    public void checkNodeConfig() {
        if (Objects.isNull(nodeConfig)) {
            log.error("AbstractYmlHandler#nodeConfig:{}异常", nodeConfig);
            throw new PlatoException("AbstractYmlHandler#nodeConfig:{}异常");
        }
    }

    public Object getHandler(String var) {
        String[] split = var.split(":");
        Object bean = SpringUtils.getBean(split[0]);
        String serviceName = bean.getClass().getName();
        return ClassUtil.methodInvoke(serviceName, split[1]);
    }
}
