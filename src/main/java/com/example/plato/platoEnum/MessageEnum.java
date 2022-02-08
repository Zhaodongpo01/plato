package com.example.plato.platoEnum;

import lombok.Getter;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 12:16 下午
 */
public enum MessageEnum {

    CLIENT_ERROR("客户端执行异常"),
    NEXT_NODE_HAS_RESULT("next节点已经有结果了"),
    SUICIDE("该节点已自杀"),
    COMING_NODE_LIMIT_CURRENT_RUN("被coming节点控制不能执行"),
    COMING_NODE_IS_NOT_PRE_NODE("强依赖节点不为空，coming节点不是强依赖节点"),
    PRE_NOT_HAS_RESULT("强依赖节点没执行完成"),
    START_MISS_ERROR("首节点为空异常"),
    UPDATE_NODE_STATUS_ERROR("修改状态异常"),
    ;

    @Getter
    private String mes;

    MessageEnum(String mes) {
        this.mes = mes;
    }
}
