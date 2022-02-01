package com.example.plato.loader.config;

import com.example.plato.exception.PlatoException;
import com.example.plato.platoEnum.NodeType;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class NodeConfig extends PlatoConfig {

    private String id;

    private String graphId;

    private String name;

    private String component;

    private String desc;

    private String inputParam;

    private NodeType type;

    private List<SubFlow> subFlows;

    private String next;

    private String pre;

    private String depend;

    private String preHandler;

    private String afterHandler;

    @Override
    void validate() {
        if (StringUtils.isBlank(id)) {
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
    }
}
