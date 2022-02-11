package com.example.plato;

import com.example.plato.holder.HandlerHolder;
import com.example.plato.holder.NodeHolder;
import com.example.plato.loader.ymlHandler.YmlAfterHandler;
import com.example.plato.loader.ymlHandler.YmlPreHandler;
import com.example.plato.loader.ymlNode.AbstractYmlNode;
import com.example.plato.util.PlatoJsonUtil;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

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
        if (event.getApplicationContext().getParent() == null) {
            Map<String, Map<String, AbstractYmlNode>> ymlNodeMap = NodeHolder.initYmlNodeMap();
            log.info("加载完毕ymlNodeMap:{}", PlatoJsonUtil.toJson(ymlNodeMap));
        }
    }
}
