package com.example.plato.exception;


/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/1/21 3:01 下午
 */
public class PlatoException extends RuntimeException {
    public PlatoException(Throwable throwable, String message) {
        super(message, throwable);
    }

    public PlatoException(String message) {
        super(message);
    }
}