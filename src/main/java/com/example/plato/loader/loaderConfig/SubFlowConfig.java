package com.example.plato.loader.loaderConfig;

import org.apache.commons.lang3.StringUtils;

import com.example.plato.exception.PlatoException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/19 12:42 下午
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubFlowConfig extends PlatoConfig{

    private String graphId;

    private String startNode;

    private String endNode;

    @Override
    public void validate() {
        if (StringUtils.isBlank(graphId)) {
            throw new PlatoException("subFlow graphId is empty");
        }
        if (StringUtils.isBlank(startNode)) {
            throw new PlatoException("subFlow startNode is empty");
        }
        if (StringUtils.isBlank(endNode)) {
            throw new PlatoException("subFlow endNode is empty");
        }
    }
}
