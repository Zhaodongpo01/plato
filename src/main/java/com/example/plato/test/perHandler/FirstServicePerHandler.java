package com.example.plato.test.perHandler;

import org.springframework.stereotype.Service;

import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.NodeRunningInfo;
import com.example.plato.util.PlatoJsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/7 10:54 上午
 */
@Slf4j
@Service
public class FirstServicePerHandler {

    public PreHandler perhandler1() {
        return new PreHandler<Integer>() {

            @Override
            public Integer paramHandle(GraphRunningInfo graphRunningInfo) {
                NodeRunningInfo uniqueIdA = graphRunningInfo.getNodeRunningInfo("uniqueIdA");
                Object data = uniqueIdA.getResultData().getData();
                log.info("perhandler1#data:{}", PlatoJsonUtil.toJson(data));
                return 10000000;
            }

            @Override
            public boolean suicide(GraphRunningInfo graphRunningInfo) {
                log.error("已经自杀，但是没完全自杀");
                return false;
            }
        };
    }
}
