package com.example.plato.runningData;

import lombok.Getter;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 12:16 下午
 */
public enum MessageEnum {

    CLIENT_ERROR("客户端代码调用执行异常"),
    NEXT_NODE_HAS_RESULT("后面节点已经有结果了"),
    SUICIDE("该节点已自杀"),
    COMING_NODE_LIMIT_CURRENT_RUN("coming节点控制不能执行"),
    PRE_NOT_HAS_RESULT("前置节点还没执行完成"),
    ;

    @Getter
    private String mes;

    MessageEnum(String mes) {
        this.mes = mes;
    }
}
