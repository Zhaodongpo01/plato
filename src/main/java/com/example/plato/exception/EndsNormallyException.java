package com.example.plato.exception;

/**
 * @author zhaodongpo
 * create  2022/5/16 10:17 下午
 * @version 1.0
 */
public abstract class EndsNormallyException extends RuntimeException {
    public EndsNormallyException() {
    }

    public EndsNormallyException(String message) {
        super(message);
    }

    public EndsNormallyException(String message, Throwable cause) {
        super(message, cause);
    }

    public EndsNormallyException(Throwable cause) {
        super(cause);
    }

    public EndsNormallyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
