package com.example.plato.util;

import java.util.function.Supplier;

import org.springframework.lang.Nullable;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/7 2:26 下午
 */
public class PlatoAssert {

    public static void notNull(@Nullable Object object, Supplier<String> messageSupplier) {
        if (object == null) {
            throw new IllegalArgumentException(nullSafeGet(messageSupplier));
        }
    }

    private static String nullSafeGet(@Nullable Supplier<String> messageSupplier) {
        return (messageSupplier != null ? messageSupplier.get() : null);
    }

}
