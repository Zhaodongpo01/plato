package com.example.plato;

import com.example.plato.annotation.AfterHandler;
import com.example.plato.annotation.NodeAnnotation;
import com.example.plato.annotation.PerHandler;
import com.example.plato.holder.NodeHolder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/6 14:06
 */
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            /*Map<String, Object> afterHandlerMap = event.getApplicationContext().getBeansWithAnnotation(AfterHandler.class);
            Map<String, Object> nodeMap = event.getApplicationContext().getBeansWithAnnotation(NodeAnnotation.class);
            Map<String, Object> perHandlerMap = event.getApplicationContext().getBeansWithAnnotation(PerHandler.class);*/
            NodeHolder.getYmlNodeMap();
        }
    }
}
