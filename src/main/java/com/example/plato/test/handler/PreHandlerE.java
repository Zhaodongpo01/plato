package com.example.plato.test.handler;

import org.springframework.stereotype.Service;

import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.ResultData;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/30 10:27 上午
 */
@Service
public class PreHandlerE implements PreHandler<Integer> {

    @Override
    public Integer paramHandle(GraphRunningInfo graphRunningInfo) {
        ResultData nodeDResult = graphRunningInfo.getResultData("nodeD");
        Object data = nodeDResult.getResult();
        return 12345678;
    }

    @Override
    public boolean suicide(GraphRunningInfo graphRunningInfo) {
        return PreHandler.super.suicide(graphRunningInfo);
    }
}
