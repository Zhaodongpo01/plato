package com.example.plato.test.handler;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.plato.handler.AfterHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.ResultData;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/30 10:22 上午
 */
@Service
public class AfterHandlerD implements AfterHandler {

    @Override
    public <R> Set<String> notShouldRunNodes(GraphRunningInfo<R> graphRunningInfo) {
        ResultData<R> nodeAResult = graphRunningInfo.getResultData("nodeA");
        R data = nodeAResult.getResult();
        return AfterHandler.super.notShouldRunNodes(graphRunningInfo);
    }
}
