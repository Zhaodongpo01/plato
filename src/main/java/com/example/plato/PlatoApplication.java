package com.example.plato;

import java.util.ArrayList;
import java.util.Objects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.google.common.collect.Lists;

@SpringBootApplication
public class PlatoApplication {

    public static void main(String[] args) {
        ArrayList<Integer> integers = Lists.newArrayList(1000, 100);
        System.out.println(integers.get(0));
        SpringApplication.run(PlatoApplication.class, args);
    }

}
