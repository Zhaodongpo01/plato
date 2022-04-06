package com.example.plato.loader.registry;


import java.util.Map;

import com.example.plato.loader.loaderConfig.GraphConfig;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/21 3:21 下午
 */
public interface GraphRegistry {

    Map<String, GraphConfig> registry();

}
