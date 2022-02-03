package com.example.plato.element;

import java.util.ArrayList;
import java.util.List;

import com.example.plato.loader.ymlNode.IYmlNode;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:18 下午
 */
@Slf4j
@Getter
public class NodeLoadByYml<P, R> extends AbstractNodeDefine {

    private String uniqueId;
    private String graphId;
    private IYmlNode iYmlNode;
    private final List<IYmlNode> nextNodes = new ArrayList<>();
    private final List<String> preNodes = new ArrayList<>();

    private NodeLoadByYml() {

    }

}
