package com.example.plato.test.service;

import org.springframework.stereotype.Service;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/3 10:25 上午
 */
@Service
public class TestService {

    public void save(String var) {
        System.out.println("svae" + var.length());
    }

}
