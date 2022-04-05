package com.example.plato.test.service.impl;


import java.util.List;

import org.springframework.stereotype.Service;

import com.example.plato.element.PlatoNode;
import com.example.plato.element.GraphManager;
import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.test.beanNode.NodeA;
import com.example.plato.test.beanNode.NodeB;
import com.example.plato.test.beanNode.NodeC;
import com.example.plato.test.beanNode.NodeD;
import com.example.plato.test.beanNode.NodeE;
import com.example.plato.test.model.FirstModel;
import com.example.plato.test.model.TestModel;
import com.example.plato.test.service.IGraphService;
import com.example.plato.util.PlatoJsonUtil;
import com.example.plato.util.TraceUtil;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/3/19 6:47 下午
 */
@Service
@Slf4j
public class GraphService implements IGraphService {

    @Override
    public void parallel() {

        PlatoNode<Integer, Void> platoNodeE = new PlatoNode.PlatoNodeBuilder<Integer, Void>()
                .setUniqueNodeId("uniqueNodeE")
                .setINodeWork(new NodeE())
                .setPreHandler(new PreHandler<Integer>() {
                    @Override
                    public Integer paramHandle(GraphRunningInfo graphRunningInfo) {
                        return 10001;
                    }
                }).build();

        PlatoNode<Void, String> platoNodeD = new PlatoNode.PlatoNodeBuilder<Void, String>()
                .setUniqueNodeId("uniqueNodeD")
                .setINodeWork(new NodeD())
                .addNext(platoNodeE, true)
                .build();

        PlatoNode<TestModel, FirstModel> platoNodeC = new PlatoNode.PlatoNodeBuilder<TestModel, FirstModel>()
                .setUniqueNodeId("uniqueNodeC")
                .setINodeWork(new NodeC())
                .setPreHandler(new PreHandler<TestModel>() {
                    @Override
                    public TestModel paramHandle(GraphRunningInfo graphRunningInfo) {
                        TestModel testModel = new TestModel();
                        testModel.setAge(10);
                        testModel.setId(108787838L);
                        testModel.setUsername("yml");
                        return testModel;
                    }
                }).addNext(platoNodeD, true).build();

        PlatoNode<List<Integer>, Boolean> platoNodeB = new PlatoNode.PlatoNodeBuilder<List<Integer>, Boolean>()
                .setUniqueNodeId("uniqueNodeB")
                .setINodeWork(new NodeB())
                .setPreHandler(new PreHandler<List<Integer>>() {
                    @Override
                    public List<Integer> paramHandle(GraphRunningInfo graphRunningInfo) {
                        log.info("testSerial获取runningInfo:{}", PlatoJsonUtil.toJson(graphRunningInfo));
                        return Lists.newArrayList(1, 2, 3, 4, 5);
                    }
                }).addNext(platoNodeC, true).build();

        PlatoNode<String, Long> platoNodeA = new PlatoNode.PlatoNodeBuilder<String, Long>()
                .setINodeWork(new NodeA())
                .setUniqueNodeId("uniqueNodeA")
                .addNext(platoNodeB, true)
                .build();

        GraphManager graphManager = new GraphManager("123");
        graphManager.run(TraceUtil.getRandomTraceId(), platoNodeA);
    }

    @Override
    public void serial() {

    }

    @Override
    public void ymlSerial() {

    }

    @Override
    public void ymlSerialBean() {

    }
}
