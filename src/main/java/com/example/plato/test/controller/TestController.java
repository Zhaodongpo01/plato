package com.example.plato.test.controller;

import com.example.plato.element.NodeManager;
import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.runningData.NodeRunningInfo;
import com.example.plato.test.model.FirstModel;
import com.example.plato.test.model.TestModel;
import com.example.plato.test.serial.NodeA;
import com.example.plato.test.serial.NodeB;
import com.example.plato.test.serial.NodeC;
import com.example.plato.test.serial.NodeD;
import com.example.plato.util.PlatoJsonUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.plato.element.NodeLoadByBean.NodeBeanBuilder;

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

    @RequestMapping("serial")
    public String testSerial() {
        NodeBeanBuilder<String, Long> nodeBeanBuilderA = NodeBeanBuilder.get().firstSetNodeBuilder("graphId", "uniqueIdA", "123333", new NodeA());
        NodeBeanBuilder<List<Integer>, Boolean> nodeBeanBuilderB = NodeBeanBuilder.get().setNodeBuilder("uniqueIdB", new NodeB());
        NodeBeanBuilder<TestModel, FirstModel> nodeBeanBuilderC = NodeBeanBuilder.get().setNodeBuilder("uniqueIdC", new NodeC());
        NodeBeanBuilder<Void, String> nodeBeanBuilderD = NodeBeanBuilder.get().setNodeBuilder("uniqueIdD", new NodeD());

        nodeBeanBuilderB.setPreHandler(new PreHandler<List<Integer>>() {
            @Override
            public List<Integer> paramHandle(GraphRunningInfo graphRunningInfo) {
                log.info("testSerial获取runningInfo:{}", PlatoJsonUtil.toJson(graphRunningInfo));
                return Lists.newArrayList(1, 2, 3, 4, 5);
            }

            @Override
            public boolean runEnable(GraphRunningInfo graphRunningInfo) {
                NodeRunningInfo uniqueIdA = graphRunningInfo.getNodeRunningInfo("uniqueIdA");
                return (Long) uniqueIdA.getResultData().getData() < 100;
            }
        });

        nodeBeanBuilderC.setPreHandler((PreHandler<TestModel>) graphRunningInfo -> {
            TestModel testModel = new TestModel();
            //Boolean resultB = (Boolean) graphRunningInfo.getNodeRunningInfo("uniqueIdB").getResultData().getData();
            testModel.setAge(100);
            testModel.setId(103_87_87_838L);
            testModel.setUsername("zhao");
            return testModel;
        });

        nodeBeanBuilderD.setPreHandler(PreHandler.VOID_PRE_HANDLER);

        NodeManager nodeManager = NodeManager.getManager()
                .linkNodes(nodeBeanBuilderA, nodeBeanBuilderB)
                .linkNodes(nodeBeanBuilderA, nodeBeanBuilderC)
                .linkNodes(nodeBeanBuilderC, nodeBeanBuilderD);
        GraphRunningInfo graphRunningInfo = nodeManager.run(100000L, TimeUnit.MILLISECONDS);
        Map<String, NodeRunningInfo> nodeRunningInfoMap = graphRunningInfo.getNodeRunningInfoMap();
        log.info("testSerial#nodeRunningInfoMap:{}", PlatoJsonUtil.toJson(nodeRunningInfoMap));
        return "success";
    }

}
