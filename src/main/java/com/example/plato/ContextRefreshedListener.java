package com.example.plato;

import java.util.Map;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.example.plato.loader.loaderConfig.GraphConfig;
import com.example.plato.loader.registry.YmlRegistry;
import com.example.plato.util.PlatoJsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/6 14:06
 */
@Slf4j
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, GraphConfig> registry = new YmlRegistry().registry();
        log.info("onApplicationEvent#registry:{}", PlatoJsonUtil.toJson(registry));
    }
}
