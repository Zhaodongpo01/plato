package com.example.plato.element;

import com.example.plato.loader.config.GraphConfig;
import com.example.plato.holder.GraphHolder;
import lombok.Getter;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/23 11:27 上午
 * 定义Graph
 */
@Getter
public class Graph {

    private String graphId;
    private String startNode;
    private String graphName;

    private Graph() {}

    private Graph(String graphId) {
        this.graphId = graphId;
    }

    void setStartNode(String startNode) {
        this.startNode = startNode;
    }

    void setGraphName(String graphName) {
        this.graphName = graphName;
    }


    /**
     * 支持yml方式实例一个Graph
     */
    public static Graph getGraphInstance(GraphConfig graphConfig) {
        if (GraphHolder.getGraph(graphConfig.getGraphId()) == null) {
            synchronized (Graph.class) {
                if (GraphHolder.getGraph(graphConfig.getGraphId()) == null) {
                    Graph graph = new Graph(graphConfig.getGraphId());
                    graph.setGraphName(graphConfig.getGraphName());
                    graph.setStartNode(graphConfig.getStartNode());
                    return GraphHolder.putGraph(graph);
                }
            }
        }
        return GraphHolder.getGraph(graphConfig.getGraphId());
    }

    /**
     * 根绝bean加载方式实例化一个Graph
     */
    public static Graph getGraphInstance(String graphId) {
        if (GraphHolder.getGraph(graphId) == null) {
            synchronized (Graph.class) {
                if (GraphHolder.getGraph(graphId) == null) {
                    Graph graph = new Graph(graphId);
                    return GraphHolder.putGraph(graph);
                }
            }
        }
        return GraphHolder.getGraph(graphId);
    }
}
