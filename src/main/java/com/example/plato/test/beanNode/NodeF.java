package com.example.plato.test.beanNode;

import org.springframework.stereotype.Service;

import com.example.plato.element.INodeWork;
import com.example.plato.runningInfo.ResultData;
import com.example.plato.test.model.NodeFModel;
import com.example.plato.util.PlatoJsonUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/4/10 12:32 下午
 */
@Slf4j
@Service
public class NodeF implements INodeWork<Integer, NodeFModel> {

    @Override
    public NodeFModel work(Integer integer) {
        NodeFModel nodeFModel = new NodeFModel();
        nodeFModel.setA((short) 10);
        nodeFModel.setCharacters(new Character[2]);
        nodeFModel.setIds(Lists.newArrayList(1L, 2L, 3L));
        nodeFModel.setMap(Maps.asMap(Sets.newHashSet(1, 2, 3), a -> ok(a)));
        return nodeFModel;
    }

    private Integer ok(Integer a) {
        return a + 10;
    }

    @Override
    public void hook(Integer integer, ResultData<NodeFModel> resultData) {
        log.info("NodeF结果resultData:{}", PlatoJsonUtil.toJson(resultData.getResult()));
    }
}
