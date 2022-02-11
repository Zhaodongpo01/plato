package com.example.plato.util;

import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 7:50 下午
 */
public class PackageScanner {

    public static Reflections scan(String packageName) {
        return new Reflections(new ConfigurationBuilder().forPackages(packageName));
    }

}
