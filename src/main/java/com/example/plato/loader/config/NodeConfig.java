package com.example.plato.loader.config;

import com.example.plato.exception.PlatoException;
import com.example.plato.platoEnum.NodeType;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/21 2:57 下午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class NodeConfig extends PlatoConfig {

    private String uniqueId;

    private String graphId;

    private String name;

    private String invokeElement;

    private String desc;

    //private String inputParam;

    private NodeType type = NodeType.BEAN;

    private List<SubFlow> subFlows;

    private String next;

    private String pre;

    private String preHandler;

    private String afterHandler;

    @Override
    void validate() {
        if (StringUtils.isBlank(uniqueId)) {
            throw new PlatoException("NodeConfig id is empty");
        }
        if (StringUtils.isBlank(name)) {
            throw new PlatoException("NodeConfig name is empty");
        }
        if (!Sets.newHashSet(NodeType.values()).contains(type)) {
            throw new PlatoException("NodeConfig type is empty");
        }
        if (StringUtils.isBlank(graphId)) {
            throw new PlatoException("NodeConfig graphId is empty");
        }
        if (StringUtils.isBlank(invokeElement) || !invokeElement.contains(":")) {
            log.error("NodeConfig invokeElement is empty:{},uniqueId:{}", invokeElement, uniqueId);
            throw new PlatoException("NodeConfig invokeElement is empty");
        }
        if (StringUtils.isNotBlank(next)) {
            try {
                Splitter.on(",").trimResults().splitToList(next);
            } catch (Exception e) {
                throw new PlatoException("NodeConfig next error");
            }
        }
        if (StringUtils.isNotBlank(pre)) {
            try {
                Splitter.on(",").trimResults().splitToList(pre);
            } catch (Exception e) {
                throw new PlatoException("NodeConfig pre error");
            }
        }
    }
}
