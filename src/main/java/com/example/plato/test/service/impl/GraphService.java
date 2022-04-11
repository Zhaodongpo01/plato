package com.example.plato.test.service.impl;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.plato.element.GraphManager;
import com.example.plato.element.PlatoNodeBuilder;
import com.example.plato.handler.PreHandler;
import com.example.plato.runningData.GraphRunningInfo;
import com.example.plato.test.beanNode.NodeA;
import com.example.plato.test.beanNode.NodeB;
import com.example.plato.test.beanNode.NodeC;
import com.example.plato.test.beanNode.NodeD;
import com.example.plato.test.beanNode.NodeE;
import com.example.plato.test.beanNode.NodeF;
import com.example.plato.test.model.FirstModel;
import com.example.plato.test.model.NodeFModel;
import com.example.plato.test.model.TestModel;
import com.example.plato.test.service.IGraphService;
import com.example.plato.util.PlatoJsonUtil;
import com.example.plato.util.TraceUtil;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * 2022/3/19 6:47 下午
 */
@Service
@Slf4j
public class GraphService implements IGraphService {

    @Autowired
    private NodeA nodeA;

    @Autowired
    private NodeB nodeB;

    @Autowired
    private NodeC nodeC;

    @Autowired
    private NodeD nodeD;

    @Autowired
    private NodeE nodeE;

    @Autowired
    private NodeF nodeF;

    private static final ThreadPoolExecutor nodeExecutor =
            new ThreadPoolExecutor(1, 10, 1000L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(1000));

    private static final ThreadPoolExecutor graphExecutor =
            new ThreadPoolExecutor(2, 20, 1000L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(1000));

    private PlatoNodeBuilder<String, Long> getPlatoNodeBuilderA() {
        return new PlatoNodeBuilder<String, Long>()
                .setINodeWork(nodeA)
                .setUniqueId("nodeA");
    }

    private PlatoNodeBuilder<List<Integer>, Boolean> getPlatoNodeBuilderB() {
        return new PlatoNodeBuilder<List<Integer>, Boolean>()
                .setINodeWork(nodeB)
                .setUniqueId("nodeB")
                .setPreHandler(new PreHandler<List<Integer>>() {
                    @Override
                    public List<Integer> paramHandle(GraphRunningInfo graphRunningInfo) {
                        return Lists.newArrayList(1, 2, 4, 4, 5);
                    }
                });
    }

    private PlatoNodeBuilder<TestModel, FirstModel> getPlatoNodeBuilderC() {
        return new PlatoNodeBuilder<TestModel, FirstModel>()
                .setINodeWork(nodeC)
                .setUniqueId("nodeC")
                .setPreHandler(new PreHandler<TestModel>() {
                    @Override
                    public TestModel paramHandle(GraphRunningInfo graphRunningInfo) {
                        TestModel testModel = new TestModel();
                        testModel.setAge(120);
                        testModel.setUsername("zhaodongpo");
                        testModel.setId(10000L);
                        return testModel;
                    }
                });
    }

    private PlatoNodeBuilder<Void, String> getPlatoNodeBuilderD() {
        return new PlatoNodeBuilder<Void, String>()
                .setINodeWork(nodeD)
                .setUniqueId("nodeD");
    }

    private PlatoNodeBuilder<Integer, Void> getPlatoNodeBuilderE() {
        return new PlatoNodeBuilder<Integer, Void>()
                .setINodeWork(nodeE)
                .setUniqueId("nodeE");
    }

    private PlatoNodeBuilder<Integer, NodeFModel> getPlatoNodeBuilderF() {
        return new PlatoNodeBuilder<Integer, NodeFModel>()
                .setINodeWork(nodeF)
                .setUniqueId("nodeF");
    }

    @Override
    public void parallel() {
        GraphManager graphManager = new GraphManager("graphId_parallel");
        PlatoNodeBuilder<String, Long> platoNodeBuilderA = getPlatoNodeBuilderA();
        PlatoNodeBuilder<List<Integer>, Boolean> platoNodeBuilderB = getPlatoNodeBuilderB();
        PlatoNodeBuilder<TestModel, FirstModel> platoNodeBuilderC = getPlatoNodeBuilderC();
        PlatoNodeBuilder<Void, String> platoNodeBuilderD = getPlatoNodeBuilderD();
        PlatoNodeBuilder<Integer, Void> platoNodeBuilderE = getPlatoNodeBuilderE();
        PlatoNodeBuilder<Integer, NodeFModel> platoNodeBuilderF = getPlatoNodeBuilderF();
        graphManager
                .linkNodes(platoNodeBuilderA, platoNodeBuilderB)
                .linkNodes(platoNodeBuilderA, platoNodeBuilderC)
                .linkNodes(platoNodeBuilderB, platoNodeBuilderD)
                .linkNodes(platoNodeBuilderC, platoNodeBuilderD)
                .linkNodes(platoNodeBuilderD, platoNodeBuilderE)
                .linkNodes(platoNodeBuilderE, platoNodeBuilderF);
        GraphRunningInfo runningInfo =
                graphManager.run(TraceUtil.getRandomTraceId(), nodeExecutor, graphExecutor, platoNodeBuilderA, 100000L,
                        TimeUnit.SECONDS);
        log.info("parallel#runningInfo:{}", PlatoJsonUtil.toJson(runningInfo.getResultDataMap()));
    }

    @Override
    public void parallelOther() {
        GraphManager graphManager = new GraphManager("graphId_parallelOther");
        PlatoNodeBuilder<String, Long> platoNodeBuilderA = getPlatoNodeBuilderA();
        PlatoNodeBuilder<List<Integer>, Boolean> platoNodeBuilderB = getPlatoNodeBuilderB();
        PlatoNodeBuilder<TestModel, FirstModel> platoNodeBuilderC = getPlatoNodeBuilderC();
        PlatoNodeBuilder<Void, String> platoNodeBuilderD = getPlatoNodeBuilderD();
        PlatoNodeBuilder<Integer, Void> platoNodeBuilderE = getPlatoNodeBuilderE();
        PlatoNodeBuilder<Integer, NodeFModel> platoNodeBuilderF = getPlatoNodeBuilderF();
        graphManager
                .linkNodes(platoNodeBuilderA, platoNodeBuilderB)
                .linkNodes(platoNodeBuilderA, platoNodeBuilderC)
                .linkNodes(platoNodeBuilderA, platoNodeBuilderD)
                .linkNodes(platoNodeBuilderB, platoNodeBuilderE)
                .linkNodes(platoNodeBuilderC, platoNodeBuilderE)
                .linkNodes(platoNodeBuilderD, platoNodeBuilderE)
                .linkNodes(platoNodeBuilderE, platoNodeBuilderF);
        GraphRunningInfo runningInfo =
                graphManager.run(TraceUtil.getRandomTraceId(), nodeExecutor, graphExecutor, platoNodeBuilderA, 100000L,
                        TimeUnit.SECONDS);
        log.info("parallelOther#runningInfo:{}", PlatoJsonUtil.toJson(runningInfo.getResultDataMap()));
    }

    @Override
    public void serial() {
        GraphManager graphManager = new GraphManager("graphId_serial");
        PlatoNodeBuilder<String, Long> platoNodeBuilderA = getPlatoNodeBuilderA();
        PlatoNodeBuilder<List<Integer>, Boolean> platoNodeBuilderB = getPlatoNodeBuilderB();
        PlatoNodeBuilder<TestModel, FirstModel> platoNodeBuilderC = getPlatoNodeBuilderC();
        PlatoNodeBuilder<Void, String> platoNodeBuilderD = getPlatoNodeBuilderD();
        PlatoNodeBuilder<Integer, Void> platoNodeBuilderE = getPlatoNodeBuilderE();
        PlatoNodeBuilder<Integer, NodeFModel> platoNodeBuilderF = getPlatoNodeBuilderF();
        graphManager
                .linkNodes(platoNodeBuilderA, platoNodeBuilderB)
                .linkNodes(platoNodeBuilderB, platoNodeBuilderC)
                .linkNodes(platoNodeBuilderC, platoNodeBuilderD)
                .linkNodes(platoNodeBuilderD, platoNodeBuilderE)
                .linkNodes(platoNodeBuilderE, platoNodeBuilderF);
        GraphRunningInfo runningInfo =
                graphManager.run(TraceUtil.getRandomTraceId(), nodeExecutor, graphExecutor, platoNodeBuilderA, 100000L,
                        TimeUnit.SECONDS);
        log.info("serial#runningInfo:{}", PlatoJsonUtil.toJson(runningInfo.getResultDataMap()));
    }

}
