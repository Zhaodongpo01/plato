package com.example.plato.exception;

import com.example.plato.element.AbstractNodeProxy;

/**
 * @author zhaodongpo
 * create  2022/5/16 10:17 下午
 * @version 1.0
 */
public class NotExpectedException extends Exception {
    public NotExpectedException(Throwable cause, AbstractNodeProxy<?, ?> wrapper) {
        super("It's should not happened Exception . wrapper is " + wrapper, cause);
    }
}