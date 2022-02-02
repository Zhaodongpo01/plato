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
public class NodeLoadByYml<P, R> {

    private String uniqueId;
    private String graphId;
    private final List<IYmlNode> nextYmlNodes = new ArrayList<>();

    private NodeLoadByYml() {

    }

    public static class NodeYmlBuilder<P, R> extends NodeLoadByYml {
    }

}
