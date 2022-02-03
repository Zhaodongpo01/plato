package com.example.plato.element.ymlNode;

import org.reflections.Reflections;

import com.example.plato.loader.config.NodeConfig;
import com.example.plato.util.PackageScanner;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 2:46 下午
 * 类 类型Node
 */
public class BeanNode extends AbstractYmlNode {

    @Override
    public IYmlNode getInstance(String scanPackage, NodeConfig nodeConfig) {
        Reflections reflections = PackageScanner.scna(scanPackage);

        return null;
    }
}
