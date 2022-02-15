package com.example.plato.test.controller;

import com.example.plato.element.GraphManager;
import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.NodeRunningInfo;
import com.example.plato.test.model.FirstModel;
import com.example.plato.test.model.TestModel;
import com.example.plato.test.node.NodeA;
import com.example.plato.test.node.NodeB;
import com.example.plato.test.node.NodeC;
import com.example.plato.test.node.NodeD;
import com.example.plato.util.Str2CodeUtil;
import com.example.plato.util.PlatoJsonUtil;
import com.example.plato.test.service.TestService;
import com.example.plato.util.TraceUtil;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.plato.element.NodeLoadByBean.NodeBeanBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/21 3:34 下午
 */
@RestController
@RequestMapping("/graph")
@Slf4j
public class TestController {

    @Autowired
    private TestService testService;

    @RequestMapping("yml")
    public String yml() {
        FirstModel firstModel = new FirstModel();
        firstModel.setIdf(1000L);
        firstModel.setName("zhaodongpo");
        GraphRunningInfo graphRunningInfo =
                GraphManager.getManager().runByYml(firstModel, "9527", 10000L, TimeUnit.SECONDS);
        log.info("yml#GraphRunningInfo:{}", PlatoJsonUtil.toJson(graphRunningInfo));
        return "success";
    }

    @RequestMapping("expression")
    public String expression() {
        Map<String, Object> map = new HashMap<>();
        map.put("testService", testService);
        map.put("var", "zhaodongpo");
        String expression = "testService.save(var)";
        Str2CodeUtil.parserString2Code(expression, map);
        return "";
    }

    @RequestMapping("serial")
    public String testSerial() {
        NodeBeanBuilder<String, Long> nodeBeanBuilderA =
                NodeBeanBuilder.get().firstSetNodeBuilder("graphId", "uniqueIdA", new NodeA());
        NodeBeanBuilder<List<Integer>, Boolean> nodeBeanBuilderB =
                NodeBeanBuilder.get().setNodeBuilder("uniqueIdB", new NodeB());
        NodeBeanBuilder<TestModel, FirstModel> nodeBeanBuilderC =
                NodeBeanBuilder.get().setNodeBuilder("uniqueIdC", new NodeC());
        NodeBeanBuilder<Void, String> nodeBeanBuilderD = NodeBeanBuilder.get().setNodeBuilder("uniqueIdD", new NodeD());
        nodeBeanBuilderB.setPreHandler(new PreHandler<List<Integer>>() {
            @Override
            public List<Integer> paramHandle(GraphRunningInfo graphRunningInfo) {
                log.info("testSerial获取runningInfo:{}", PlatoJsonUtil.toJson(graphRunningInfo));
                return Lists.newArrayList(1, 2, 3, 4, 5);
            }

            @Override
            public boolean suicide(GraphRunningInfo graphRunningInfo) {
                NodeRunningInfo uniqueIdA = graphRunningInfo.getNodeRunningInfo("uniqueIdA");
                return (Long) uniqueIdA.getResultData().getData() > 100;
            }
        });
        nodeBeanBuilderC.setPreHandler(new PreHandler() {
            @Override
            public Object paramHandle(GraphRunningInfo graphRunningInfo) {
                TestModel testModel = new TestModel();
                testModel.setAge(10);
                testModel.setId(103_87_87_838L);
                testModel.setUsername("wb_liuyanmei");
                return testModel;
            }
        });
        nodeBeanBuilderD.setPreHandler(PreHandler.DEFAULT_PRE_HANDLER);

        GraphManager graphManager = GraphManager.getManager()
                .linkNodes(nodeBeanBuilderA, nodeBeanBuilderB)
                .linkNodes(nodeBeanBuilderA, nodeBeanBuilderC)
                .linkNodes(nodeBeanBuilderB, nodeBeanBuilderD)
                .linkNodes(nodeBeanBuilderC, nodeBeanBuilderD, false);
        GraphRunningInfo graphRunningInfo =
                graphManager.run(TraceUtil.getRandomTraceId(), 100000L, TimeUnit.MILLISECONDS);
        Map<String, NodeRunningInfo> nodeRunningInfoMap = graphRunningInfo.getNodeRunningInfoMap();
        log.info("testSerial#nodeRunningInfoMap:{}", PlatoJsonUtil.toJson(nodeRunningInfoMap));
        return "success";
    }
}
