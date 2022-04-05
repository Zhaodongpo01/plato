package com.example.plato.test.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

    @RequestMapping("/parallel")
    public String parallel() throws InterruptedException {
        iGraphService.parallel();
        return "SUCCESS";
    }

}
