package com.example.plato.loader.ymlNode;

import org.reflections.Reflections;

import com.example.plato.util.PackageScanner;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:46 下午
 * 类 类型Node
 */
public class BeanYmlNode extends AbstractYmlNode {

    private String inputParam;

    @Override
    public IYmlNode getInstance(String scanPackage) {
        Reflections reflections = PackageScanner.scna(scanPackage);
        return null;
    }
}
