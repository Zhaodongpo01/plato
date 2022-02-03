package com.example.plato.loader.ymlNode;

import java.util.List;

import lombok.Data;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 7:13 下午
 */
@Data
public abstract class AbstractYmlNode implements IYmlNode {

    private String name;
    private String uniqueId;
    private String graphId;
    private String component;
    private String scanPackage;

    private List<IYmlNode> nextNodes;
    private List<IYmlNode> preNodes;
    private String preHandler;
    private String afterHandler;


}
