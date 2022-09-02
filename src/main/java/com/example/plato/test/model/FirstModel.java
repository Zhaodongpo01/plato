package com.example.plato.test.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/1 10:10
 */
@Data
public class FirstModel {

    private Long idf;

    private String name;

    private Map<NodeFModel, List<Long>> fModel = new ConcurrentHashMap<>();

}
