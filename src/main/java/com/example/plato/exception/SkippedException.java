package com.example.plato.exception;

/**
 * @author zhaodongpo
 * create  2022/5/16 10:17 下午
 * @version 1.0
 */
public class SkippedException extends EndsNormallyException {
    public SkippedException() {
        this(null);
    }

    public SkippedException(String message) {
        super(message);
    }
}