package com.example.plato.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.plato.element.DefaultGraph;
import com.example.plato.element.GraphManager;
import com.example.plato.element.NodeWorkBuilder;
import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.PreHandler;
import com.example.plato.platoEnum.RelationEnum;
import com.example.plato.runningInfo.GraphRunningInfo;
import com.example.plato.runningInfo.ResultData;
import com.example.plato.test.beanNode.NodeA;
import com.example.plato.test.beanNode.NodeB;
import com.example.plato.test.beanNode.NodeC;
import com.example.plato.test.beanNode.NodeD;
import com.example.plato.test.model.FirstModel;
import com.example.plato.test.model.TestModel;
import com.example.plato.util.PlatoJsonUtil;

import io.netty.util.HashedWheelTimer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * create  2022/5/17 8:02 下午
 * @version 1.0
 */
@Service
@Slf4j
public class GraphService {

    @Autowired
    private NodeA nodeA = new NodeA();

    @Autowired
    private NodeB nodeB = new NodeB();

    @Autowired
    private NodeC nodeC = new NodeC();

    @Autowired
    private NodeD nodeD = new NodeD();

    private final ExecutorService executorService =
            new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 15, 1000, 1000L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(100));


    public void serial() {
        for (int i = 0; i < 1; i++) {
            new Thread(() -> fuction()).start();
        }
    }

    private void fuction() {

        delayTimer();

        String graphId = "graphIdSerial";
        AfterHandler afterHandler = new AfterHandler() {
            @Override
            public <V> Set<String> notShouldRunNodes(GraphRunningInfo<V> graphRunningInfo) {
                return AfterHandler.super.notShouldRunNodes(graphRunningInfo);
            }
        };
        NodeWorkBuilder<String, Long> nodeProxyBuilderA =
                new NodeWorkBuilder("nodeA", nodeA, graphId, 100000, null, afterHandler);

        PreHandler<List<Integer>> preHandlerB = new PreHandler<List<Integer>>() {
            @Override
            public List<Integer> paramHandle(GraphRunningInfo graphRunningInfo) {
                ResultData nodeA1 = graphRunningInfo.getResultData("nodeA");
                Long result = (Long) nodeA1.getResult();
                List<Integer> list = new ArrayList<>();
                list.add(result.intValue());
                return list;
            }
        };
        NodeWorkBuilder<List<Integer>, Boolean> nodeProxyBuilderB =
                new NodeWorkBuilder("nodeB", nodeB, graphId, 100000L, preHandlerB, null);

        PreHandler<TestModel> preHandlerC = new PreHandler<TestModel>() {
            @Override
            public TestModel paramHandle(GraphRunningInfo graphRunningInfo) {
                ResultData nodeA1 = graphRunningInfo.getResultData("nodeA");
                Long result = (Long) nodeA1.getResult();
                TestModel testModel = new TestModel();
                testModel.setId(result);
                testModel.setUsername("zhaodongpo");
                testModel.setAge(100);
                return testModel;
            }
        };
        NodeWorkBuilder<TestModel, FirstModel> nodeProxyBuilderC =
                new NodeWorkBuilder("nodeC", nodeC, graphId, 100000L, preHandlerC, null);

        PreHandler<Void> preHandlerD = new PreHandler<Void>() {
            @Override
            public Void paramHandle(GraphRunningInfo graphRunningInfo) {
                log.info("NodeD节点的PreHandler:{}", PlatoJsonUtil.toJson(graphRunningInfo));
                return null;
            }
        };
        NodeWorkBuilder<Void, String> nodeProxyBuilderD =
                new NodeWorkBuilder("nodeD", nodeD, graphId, 100000L, preHandlerD, null);

        DefaultGraph defaultGraph = new DefaultGraph();
        defaultGraph.putRelation(nodeProxyBuilderA, RelationEnum.STRONG_RELATION, nodeProxyBuilderB);
        defaultGraph.putRelation(nodeProxyBuilderA, RelationEnum.STRONG_RELATION, nodeProxyBuilderC);
        defaultGraph.putRelation(nodeProxyBuilderB, RelationEnum.STRONG_RELATION, nodeProxyBuilderD);
        defaultGraph.putRelation(nodeProxyBuilderC, RelationEnum.STRONG_RELATION, nodeProxyBuilderD);

        GraphManager graphManager = new GraphManager(graphId);
        GraphRunningInfo nodeAParam =
                graphManager.run(executorService, UUID.randomUUID().toString(), nodeProxyBuilderA, 10000000L);
        log.info("结果:{}", PlatoJsonUtil.toJson(nodeAParam));
    }

    private void delayTimer() {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        HashedWheelTimer timer = new HashedWheelTimer(1, TimeUnit.MILLISECONDS, 16);
        //把任务加到HashedWheelTimer里，到了延迟的时间就会自动执行
        timer.newTimeout((timeout) -> {
            log.info("task1 execute");
            countDownLatch.countDown();
        }, 500, TimeUnit.MILLISECONDS);

        timer.newTimeout((timeout) -> {
            log.info("task2 execute");
            countDownLatch.countDown();
        }, 2, TimeUnit.MILLISECONDS);

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        timer.stop();
    }

}
