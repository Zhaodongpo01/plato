package com.example.plato.util;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.ObjectUtils;

import com.example.plato.exception.PlatoException;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/7 2:26 下午
 */
public class PlatoAssert {

    public static void notNull(Supplier<String> supplier, Object... objects) {
        if (ObjectUtils.anyNull(objects)) {
            throw new PlatoException(getErrorMes(supplier));
        }
    }

    public static <T> T nullGet(Supplier<T> supplier, T defaultVal, Object... objects) {
        if (ObjectUtils.anyNull(objects)) {
            return supplier.get();
        }
        return defaultVal;
    }

    private static <T> T getErrorMes(Supplier<T> supplier) {
        if (Optional.ofNullable(supplier).isPresent()) {
            return supplier.get();
        }
        return null;
    }

}
