package com.example.plato.test.ymlHandler;

import org.springframework.stereotype.Service;

import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.NodeRunningInfo;
import com.example.plato.util.PlatoJsonUtil;

import lombok.extern.slf4j.Slf4j;

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
                log.info("调用自杀接口返回false");
                return false;
            }
        };
    }

    public PreHandler perhandler2() {
        return PreHandler.VOID_PRE_HANDLER;
    }

}