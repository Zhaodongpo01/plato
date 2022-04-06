package com.example.plato.handler;

import java.util.Map;

import com.example.plato.element.PlatoNodeProxy;
import com.example.plato.runningData.ResultData;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/4/1 12:23 上午
 */
public interface PreHandler<P> {

    default P paramHandle(Map<String, PlatoNodeProxy> forParamUseProxies) {
        return null;
    }

    default boolean suicide(Map<String, ResultData> resultDataMap) {
        return false;
    }


}
