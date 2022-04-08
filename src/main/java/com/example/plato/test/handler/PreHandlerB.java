package com.example.plato.test.handler;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.ResultData;
import com.example.plato.util.PlatoJsonUtil;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/30 10:27 上午
 */
@Service
@Slf4j
public class PreHandlerB implements PreHandler<List<Integer>> {

    @Override
    public List<Integer> paramHandle(GraphRunningInfo graphRunningInfo) {
        Map<String, ResultData> resultDataMap = graphRunningInfo.getResultDataMap();
        return Lists.newArrayList(1, 2, 34, 4, 4, 5);
    }

    @Override
    public boolean suicide(GraphRunningInfo graphRunningInfo) {
        return PreHandler.super.suicide(graphRunningInfo);
    }
}
