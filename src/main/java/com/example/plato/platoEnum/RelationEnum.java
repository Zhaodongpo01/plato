package com.example.plato.platoEnum;

/**
 * @author zhaodongpo
 * create  2022/5/17 4:11 下午
 * @version 1.0
 */
public enum RelationEnum {

    STRONG_RELATION("后节点强关联前节点"),
    WEAK_RELATION("后节点弱关联前节点"),
    ;

    private String desc;

    RelationEnum(String desc) {
        this.desc = desc;
    }

}
