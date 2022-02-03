package com.example.plato.element.ymlNode;

import com.example.plato.loader.config.NodeConfig;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:45 下午
 */
public interface IYmlNode {

    IYmlNode getInstance(String scanPackage, NodeConfig nodeConfig);

}
