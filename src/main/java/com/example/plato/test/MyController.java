package com.example.plato.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhaodongpo
 * create  2022/5/17 8:01 下午
 * @version 1.0
 */
@RestController
@RequestMapping("/graph")
public class MyController {

    @Autowired
    private GraphService graphService;

    @RequestMapping("/serial")
    public String serial() {
        graphService.serial();
        return "SUCCESS";
    }

}
