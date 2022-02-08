package com.example.plato.test.serial.second;

import com.example.plato.handler.INodeWork;
import com.example.plato.runningData.ResultData;
import com.example.plato.test.model.FirstModel;
import com.example.plato.util.PlatoJsonUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/8 4:45 下午
 */
@Slf4j
public class Nc implements INodeWork<FirstModel, String> {

    @Override
    public String work(FirstModel firstModel) throws InterruptedException {
        log.info("Nc#work:{}", PlatoJsonUtil.toJson(firstModel));
        return PlatoJsonUtil.toJson(firstModel);
    }

    @Override
    public void hook(FirstModel firstModel, ResultData<String> resultData) {
        log.info("Nc#hook 参数:{},{}", PlatoJsonUtil.toJson(firstModel), resultData.getData());
    }
}
