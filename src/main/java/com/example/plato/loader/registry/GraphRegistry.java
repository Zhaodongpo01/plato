package com.example.plato.loader.registry;

import com.example.plato.loader.config.GraphConfig;

import java.util.Map;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/21 3:21 下午
 */
public interface GraphRegistry {

    Map<String, GraphConfig> registry();

}
