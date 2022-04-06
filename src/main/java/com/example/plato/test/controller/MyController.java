package com.example.plato.test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.plato.test.service.IGraphService;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/4/1 10:22 上午
 */
@Controller
@RequestMapping("/graph")
public class MyController {

    @Autowired
    private IGraphService iGraphService;

    @PostMapping("/parallel")
    public String parallel() {
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> iGraphService.parallel()).start();
        }
        //iGraphService.parallel();
        return "SUCCESS";
    }

    @PostMapping("/serial")
    public String serial() {
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> iGraphService.serial()).start();
        }
        //iGraphService.parallel();
        return "SUCCESS";
    }

}
