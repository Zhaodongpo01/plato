package com.example.plato.test.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.example.plato.element.GraphManager;
import com.example.plato.element.WorkerWrapper;
import com.example.plato.handler.PreHandler;
import com.example.plato.test.beanNode.NodeA;
import com.example.plato.test.beanNode.NodeB;
import com.example.plato.test.beanNode.NodeC;
import com.example.plato.test.beanNode.NodeD;
import com.example.plato.test.model.FirstModel;
import com.example.plato.test.model.TestModel;
import com.example.plato.test.service.IGraphService;
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

    private static final ThreadPoolExecutor COMMON_POOL = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    @Override
    public void parallel() {

        NodeA nodeA = new NodeA();
        NodeB nodeB = new NodeB();
        NodeC nodeC = new NodeC();
        NodeD nodeD = new NodeD();

        WorkerWrapper<Void, String> platoNodeProxyD =
                new WorkerWrapper.Builder<Void, String>().setINodeWork(nodeD).setUniqueId("nodeD").build();

        WorkerWrapper<TestModel, FirstModel> platoNodeProxyC = new WorkerWrapper.Builder<TestModel, FirstModel>()
                .setINodeWork(nodeC)
                .setUniqueId("nodeC")
                .next(platoNodeProxyD)
                .setPreHandler(new PreHandler<TestModel>() {
                    @Override
                    public TestModel paramHandle(Map map) {
                        TestModel testModel = new TestModel();
                        testModel.setAge(120);
                        testModel.setUsername("zhaodongpo");
                        testModel.setId(10000L);
                        return testModel;
                    }
                }).build();

        WorkerWrapper<List<Integer>, Boolean> platoNodeProxyB = new WorkerWrapper.Builder<List<Integer>, Boolean>()
                .setINodeWork(nodeB)
                .setUniqueId("nodeB")
                .next(platoNodeProxyD)
                .setPreHandler(new PreHandler<List<Integer>>() {
                    @Override
                    public List<Integer> paramHandle(Map map) {
                        return Lists.newArrayList(1, 2, 4, 4, 5);
                    }
                }).build();

        WorkerWrapper<String, Long> platoNodeProxyA = new WorkerWrapper.Builder<String, Long>()
                .setINodeWork(nodeA)
                .setUniqueId("nodeA")
                .next(platoNodeProxyB, platoNodeProxyC)
                .build();

        GraphManager graphManager = new GraphManager("grapIds");
        graphManager.run(TraceUtil.getRandomTraceId(), COMMON_POOL, platoNodeProxyA, 100000L, TimeUnit.SECONDS);
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
