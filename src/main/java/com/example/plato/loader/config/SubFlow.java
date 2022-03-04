package com.example.plato.loader.config;

import com.example.plato.exception.PlatoException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/21 3:06 下午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubFlow extends PlatoConfig {

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
