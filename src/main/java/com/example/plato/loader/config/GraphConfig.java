package com.example.plato.loader.config;

import com.example.plato.exception.PlatoException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/21 2:32 下午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphConfig extends PlatoConfig {

    private String graphId;

    private String graphName;

    private String graphDesc;

    private String startNode;

    private String scanPackage;

    private List<NodeConfig> nodes;

    @Override
    void validate() {
        if (StringUtils.isBlank(graphId)) {
            throw new PlatoException("GraphConfig graphId is empty");
        }
        if (StringUtils.isBlank(graphName)) {
            throw new PlatoException("GraphConfig graphName is empty");
        }
        if (CollectionUtils.isEmpty(nodes)) {
            throw new PlatoException("GraphConfig nodes is empty");
        }
        if (StringUtils.isBlank(scanPackage)) {
            throw new PlatoException("GraphConfig scanPackage is empty");
        }
    }
}
