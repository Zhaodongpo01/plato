package com.example.plato.platoEnum;

import lombok.Getter;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/2 12:16 下午
 */
public enum MessageEnum {

    CLIENT_ERROR("客户端执行异常"),
    UPDATE_NODE_STATUS_ERROR("修改状态异常"),
    ;

    @Getter
    private String mes;

    MessageEnum(String mes) {
        this.mes = mes;
    }
}
