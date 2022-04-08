package com.example.plato.test.service.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.example.plato.element.GraphManager;
import com.example.plato.element.PlatoNodeProxy.PlatoNodeBuilder;
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

    //private static final ThreadPoolExecutor COMMON_POOL = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    @Override
    public void parallel() {

        NodeA nodeA = new NodeA();
        NodeB nodeB = new NodeB();
        NodeC nodeC = new NodeC();
        NodeD nodeD = new NodeD();
        NodeE nodeE = new NodeE();

        PlatoNodeBuilder<Integer, Void> platoNodeProxyE =
                new PlatoNodeBuilder<Integer, Void>().setINodeWork(nodeE).setUniqueId("nodeE");

        PlatoNodeBuilder<Void, String> platoNodeProxyD =
                new PlatoNodeBuilder<Void, String>().setINodeWork(nodeD).setUniqueId("nodeD");

        PlatoNodeBuilder<TestModel, FirstModel> platoNodeProxyC = new PlatoNodeBuilder<TestModel, FirstModel>()
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

        PlatoNodeBuilder<List<Integer>, Boolean> platoNodeProxyB = new PlatoNodeBuilder<List<Integer>, Boolean>()
                .setINodeWork(nodeB)
                .setUniqueId("nodeB")
                .setPreHandler(new PreHandler<List<Integer>>() {
                    @Override
                    public List<Integer> paramHandle(GraphRunningInfo graphRunningInfo) {
                        return Lists.newArrayList(1, 2, 4, 4, 5);
                    }
                });

        PlatoNodeBuilder<String, Long> platoNodeProxyA = new PlatoNodeBuilder<String, Long>()
                .setINodeWork(nodeA)
                .setUniqueId("nodeA");

        GraphManager graphManager = new GraphManager("grapIds");
        graphManager.linkNodes(platoNodeProxyA, platoNodeProxyB).linkNodes(platoNodeProxyA, platoNodeProxyC)
                .linkNodes(platoNodeProxyB, platoNodeProxyD).linkNodes(platoNodeProxyC, platoNodeProxyD)
                .linkNodes(platoNodeProxyD, platoNodeProxyE);
        GraphRunningInfo runningInfo =
                graphManager.run(TraceUtil.getRandomTraceId(), platoNodeProxyA, 100000L, TimeUnit.SECONDS);
        log.info("parallel#runningInfo:{}", PlatoJsonUtil.toJson(runningInfo.getResultDataMap()));
    }

    @Override
    public void serial() {

        NodeA nodeA = new NodeA();
        NodeB nodeB = new NodeB();
        NodeC nodeC = new NodeC();
        NodeD nodeD = new NodeD();

        PlatoNodeBuilder<Void, String> platoNodeProxyD =
                new PlatoNodeBuilder<Void, String>().setINodeWork(nodeD).setUniqueId("nodeD");

        PlatoNodeBuilder<TestModel, FirstModel> platoNodeProxyC = new PlatoNodeBuilder<TestModel, FirstModel>()
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

        PlatoNodeBuilder<List<Integer>, Boolean> platoNodeProxyB = new PlatoNodeBuilder<List<Integer>, Boolean>()
                .setINodeWork(nodeB)
                .setUniqueId("nodeB")
                .setPreHandler(new PreHandler<List<Integer>>() {
                    @Override
                    public List<Integer> paramHandle(GraphRunningInfo graphRunningInfo) {
                        return Lists.newArrayList(1, 2, 4, 4, 5);
                    }
                });

        PlatoNodeBuilder<String, Long> platoNodeProxyA = new PlatoNodeBuilder<String, Long>()
                .setINodeWork(nodeA)
                .setUniqueId("nodeA");

        GraphManager graphManager = new GraphManager("grapIds");
        graphManager.linkNodes(platoNodeProxyC, platoNodeProxyD).linkNodes(platoNodeProxyB, platoNodeProxyC)
                .linkNodes(platoNodeProxyA, platoNodeProxyB);
        GraphRunningInfo runningInfo =
                graphManager.run(TraceUtil.getRandomTraceId(), platoNodeProxyA, 100000L, TimeUnit.SECONDS);
        log.info("serial#runningInfo:{}", PlatoJsonUtil.toJson(runningInfo.getResultDataMap()));
    }

    @Override
    public void testt() {
        NodeA nodeA = new NodeA();
        NodeB nodeB = new NodeB();
        NodeC nodeC = new NodeC();
        NodeD nodeD = new NodeD();

        PlatoNodeBuilder<Void, String> platoNodeBuilderD =
                new PlatoNodeBuilder<Void, String>().setINodeWork(nodeD).setUniqueId("nodeD");

        PlatoNodeBuilder<TestModel, FirstModel> platoNodeBuilderC = new PlatoNodeBuilder<TestModel, FirstModel>()
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

        PlatoNodeBuilder<List<Integer>, Boolean> platoNodeBuilderB = new PlatoNodeBuilder<List<Integer>, Boolean>()
                .setINodeWork(nodeB)
                .setUniqueId("nodeB")
                .setPreHandler(new PreHandler<List<Integer>>() {
                    @Override
                    public List<Integer> paramHandle(GraphRunningInfo graphRunningInfo) {
                        return Lists.newArrayList(1, 2, 4, 4, 5);
                    }
                });

        PlatoNodeBuilder<String, Long> platoNodeProxyA = new PlatoNodeBuilder<String, Long>()
                .setINodeWork(nodeA)
                .setUniqueId("nodeA");

        GraphManager graphManager = new GraphManager("grapIds");
        graphManager.linkNodes(platoNodeBuilderC, platoNodeBuilderD).linkNodes(platoNodeBuilderB, platoNodeBuilderC)
                .linkNodes(platoNodeProxyA, platoNodeBuilderB);
        GraphRunningInfo runningInfo =
                graphManager.run(TraceUtil.getRandomTraceId(), platoNodeProxyA, 100000L, TimeUnit.SECONDS);
        log.info("serial#runningInfo:{}", PlatoJsonUtil.toJson(runningInfo.getResultDataMap()));
    }

    @Override
    public void yml() {
        GraphManager graphManager = new GraphManager("9526");
        GraphRunningInfo graphRunningInfo =
                graphManager.run(TraceUtil.getRandomTraceId(), "nodeA", 10000L, TimeUnit.SECONDS);
        log.info("yml#graphRunningInfo:{}", PlatoJsonUtil.toJson(graphRunningInfo));
    }

    @Override
    public void parallel1() {
        NodeA nodeA = new NodeA();
        NodeB nodeB = new NodeB();
        NodeC nodeC = new NodeC();
        NodeD nodeD = new NodeD();

        PlatoNodeBuilder<Void, String> platoNodeProxyD =
                new PlatoNodeBuilder<Void, String>().setINodeWork(nodeD).setUniqueId("nodeD");

        PlatoNodeBuilder<TestModel, FirstModel> platoNodeProxyC = new PlatoNodeBuilder<TestModel,
                FirstModel>()
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

        PlatoNodeBuilder<List<Integer>, Boolean> platoNodeProxyB = new PlatoNodeBuilder<List<Integer>,
                Boolean>()
                .setINodeWork(nodeB)
                .setUniqueId("nodeB")
                .checkNextResult(false)
                .next(platoNodeProxyD, false)
                .setPreHandler(new PreHandler<List<Integer>>() {
                    @Override
                    public List<Integer> paramHandle(GraphRunningInfo graphRunningInfo) {
                        return Lists.newArrayList(1, 2, 4, 4, 5);
                    }
                });

        PlatoNodeBuilder<String, Long> platoNodeProxyA = new PlatoNodeBuilder<String, Long>()
                .setINodeWork(nodeA)
                .setUniqueId("nodeA")
                .next(platoNodeProxyB, platoNodeProxyC);

        GraphManager graphManager = new GraphManager("grapIds");
        graphManager.linkNodes(platoNodeProxyC, platoNodeProxyD, false)
                .linkNodes(platoNodeProxyB, platoNodeProxyD, false)
                .linkNodes(platoNodeProxyA, platoNodeProxyD).linkNodes(platoNodeProxyA, platoNodeProxyB)
                .linkNodes(platoNodeProxyA, platoNodeProxyC);
        GraphRunningInfo runningInfo =
                graphManager.run(TraceUtil.getRandomTraceId(), platoNodeProxyA, 100000L, TimeUnit.SECONDS);
        log.info("parallel#runningInfo:{}", PlatoJsonUtil.toJson(runningInfo.getResultDataMap()));
    }
}
