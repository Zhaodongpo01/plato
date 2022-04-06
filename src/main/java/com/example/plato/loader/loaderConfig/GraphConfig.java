package com.example.plato.loader.loaderConfig;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.example.plato.exception.PlatoException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/19 12:41 下午
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

    private List<NodeConfig> nodes;

    @Override
    void validate() {
        if (StringUtils.isBlank(startNode)) {
            throw new PlatoException("GraphConfig startNode is empty");
        }
        if (StringUtils.isBlank(graphId)) {
            throw new PlatoException("GraphConfig graphId is empty");
        }
        if (StringUtils.isBlank(graphName)) {
            throw new PlatoException("GraphConfig graphName is empty");
        }
        if (CollectionUtils.isEmpty(nodes)) {
            throw new PlatoException("GraphConfig nodes is empty");
        }
    }
}
