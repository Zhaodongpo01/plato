package com.example.plato.element.ymlNode;

import java.util.List;

import com.example.plato.loader.config.NodeConfig;
import com.example.plato.loader.config.SubFlow;

import lombok.Data;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:47 下午
 * 子流程类型Node
 */
@Data
public class SubflowNode extends AbstractYmlNode {

    @Override
    public IYmlNode getInstance(String scanPackage, NodeConfig nodeConfig) {
        List<SubFlow> subFlows = nodeConfig.getSubFlows();

        return null;
    }
}
