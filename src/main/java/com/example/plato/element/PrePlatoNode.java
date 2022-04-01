package com.example.plato.element;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/31 11:17 下午
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrePlatoNode {
    private PlatoNode<?, ?> platoNode;
    private boolean must = true;
}
