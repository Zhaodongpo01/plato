package com.example.plato.loader.ymlNode;

import java.util.List;

import com.example.plato.loader.config.SubFlow;

import lombok.Data;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:47 下午
 * 子流程类型Node
 */
@Data
public class SubflowYmlNode extends AbstractYmlNode {

    private List<SubFlow> subFlows;

    @Override
    public IYmlNode getInstance(String scanPackage) {
        return null;
    }
}
