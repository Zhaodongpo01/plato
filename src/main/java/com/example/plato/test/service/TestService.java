package com.example.plato.test.service;

import com.example.plato.annotation.AfterHandler;
import org.springframework.stereotype.Service;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/3 10:25 上午
 */
@AfterHandler(value = "afterTestService")
public class TestService {

    public void save(String var) {
        System.out.println("svae" + var.length());
    }

}
