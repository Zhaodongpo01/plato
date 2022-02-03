package com.example.plato.loader.ymlNode;

import org.reflections.Reflections;

import com.example.plato.util.PackageScanner;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:46 下午
 * 条件类型Node
 */
public class ConditionYmlNode extends AbstractYmlNode {

    @Override
    public IYmlNode getInstance(String scanPackage) {
        Reflections scna = PackageScanner.scna(scanPackage);

        return null;
    }
}
