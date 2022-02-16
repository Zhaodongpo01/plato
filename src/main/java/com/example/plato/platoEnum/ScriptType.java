package com.example.plato.platoEnum;

import lombok.Getter;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/13 10:22 下午
 */
public enum ScriptType {
    PYTHON("python"),
    SHELL("shell"),
    ;

    @Getter
    String scriptType;

    ScriptType(String scriptType) {
        this.scriptType = scriptType;
    }
}
