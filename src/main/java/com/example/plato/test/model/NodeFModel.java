package com.example.plato.test.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Data;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/4/10 12:32 下午
 */
@Data
public class NodeFModel {

    private List<Long> ids = new ArrayList<>();

    private Map<Integer, Integer> map = new ConcurrentHashMap<>();

    private Short a = 1;

    private Character[] characters;

    {
        characters = new Character[10];
    }
}
