package com.example.plato.util;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.example.plato.exception.PlatoException;

/**
 * @author zhaodongpo
 * @version 1.0
 * @date 2022/2/7 2:26 下午
 */
public class PlatoAssert {

    public static void nullException(Supplier<String> supplier, Object... objects) {
        if (ObjectUtils.anyNull(objects)) {
            throw new PlatoException(getErrorMes(supplier));
        }
    }

    public static void emptyException(Supplier<String> supplier, String... str) {
        if (StringUtils.isAnyBlank(str)) {
            throw new PlatoException(getErrorMes(supplier));
        }
    }

    public static <E> void emptyException(Supplier<String> supplier, Collection<E> collection) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new PlatoException(getErrorMes(supplier));
        }
    }

    public static <K, V> void emptyException(Supplier<String> supplier, Map<K, V> map) {
        if (MapUtils.isEmpty(map)) {
            throw new PlatoException(getErrorMes(supplier));
        }
    }

    public static <K, V> void notEmptyException(Supplier<String> supplier, Map<K, V> map) {
        if (MapUtils.isNotEmpty(map)) {
            throw new PlatoException(getErrorMes(supplier));
        }
    }

    public static <E> void notEmptyException(Supplier<String> supplier, Collection<E> collection) {
        if (CollectionUtils.isNotEmpty(collection)) {
            throw new PlatoException(getErrorMes(supplier));
        }
    }

    private static <T> T getErrorMes(Supplier<T> supplier) {
        if (Optional.ofNullable(supplier).isPresent()) {
            return supplier.get();
        }
        return null;
    }

}
