package com.example.plato.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.example.plato.element.DefaultGraph;
import com.example.plato.element.GraphManager;
import com.example.plato.element.NodeProxyBuilder;
import com.example.plato.handler.AfterHandler;
import com.example.plato.handler.PreHandler;
import com.example.plato.platoEnum.RelationEnum;
import com.example.plato.runningInfo.GraphRunningInfo;
import com.example.plato.runningInfo.ResultData;
import com.example.plato.test.beanNode.NodeA;
import com.example.plato.test.beanNode.NodeB;
import com.example.plato.test.beanNode.NodeC;
import com.example.plato.test.model.FirstModel;
import com.example.plato.test.model.TestModel;
import com.example.plato.util.PlatoJsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * create  2022/5/17 8:02 下午
 * @version 1.0
 */
@Service
@Slf4j
public class GraphService {

    private final ExecutorService executorService =
            new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 5, 100, 1000L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(100));

    public void serial() {

        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                String graphId = "graphIdSerial";
                NodeA nodeA = new NodeA();
                AfterHandler afterHandler = new AfterHandler() {
                    @Override
                    public <V> Set<String> notShouldRunNodes(GraphRunningInfo<V> graphRunningInfo) {
                        return AfterHandler.super.notShouldRunNodes(graphRunningInfo);
                    }
                };
                NodeProxyBuilder<String, Long> nodeProxyBuilderA =
                        new NodeProxyBuilder("nodeA", nodeA, graphId, 100000L, null, afterHandler);


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
                NodeB nodeB = new NodeB();
                NodeProxyBuilder<List<Integer>, Boolean> nodeProxyBuilderB =
                        new NodeProxyBuilder("nodeB", nodeB, graphId, 100000L, preHandlerB, null);


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
                NodeC nodeC = new NodeC();
                NodeProxyBuilder<TestModel, FirstModel> nodeProxyBuilderC =
                        new NodeProxyBuilder("nodeC", nodeC, graphId, 100000L, preHandlerC, null);

                DefaultGraph relationEnumDefaultGraph = new DefaultGraph();
                relationEnumDefaultGraph.putRelation(nodeProxyBuilderA, RelationEnum.STRONG_RELATION,
                        nodeProxyBuilderB);
                relationEnumDefaultGraph.putRelation(nodeProxyBuilderA, RelationEnum.STRONG_RELATION,
                        nodeProxyBuilderC);
                nodeProxyBuilderA.setGraph(relationEnumDefaultGraph);
                nodeProxyBuilderB.setGraph(relationEnumDefaultGraph);
                nodeProxyBuilderC.setGraph(relationEnumDefaultGraph);
                GraphManager graphManager = new GraphManager(graphId);
                GraphRunningInfo nodeAParam =
                        graphManager.run(executorService, UUID.randomUUID().toString(), nodeProxyBuilderA, 10000000L);
                log.info("结果:{}", PlatoJsonUtil.toJson(nodeAParam));
            }).start();
        }
    }

}
