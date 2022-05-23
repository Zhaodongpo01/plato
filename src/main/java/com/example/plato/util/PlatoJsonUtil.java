package com.example.plato.util;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS;
import static com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 * @author zhaodongpo
 * @version 1.0
 * create 2021/12/26 11:50 上午
 */
public class PlatoJsonUtil {

    private PlatoJsonUtil() {
        throw new UnsupportedOperationException();
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final DefaultPrettyPrinter PRETTY_PRINTER = new DefaultPrettyPrinter();

    static {
        MAPPER.enable(ALLOW_COMMENTS);
        MAPPER.enable(STRICT_DUPLICATE_DETECTION);
        MAPPER.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        MAPPER.registerModule(new ParameterNamesModule());
        DefaultIndenter indenter = new DefaultIndenter("  ", DefaultIndenter.SYS_LF);
        PRETTY_PRINTER.indentObjectsWith(indenter);
        PRETTY_PRINTER.indentArraysWith(indenter);
    }

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T fromJson(String json, Class<T> valueType) {
        try {
            return MAPPER.readValue(json, valueType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T fromJson(String json, JavaType javaType) {
        try {
            return MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <T> T fromJson(byte[] json, Class<T> valueType) {
        try {
            return MAPPER.readValue(json, valueType);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <E, T extends Collection<E>> T ofJsonCollection(String json,
            Class<? extends Collection> collectionType, Class<E> valueType) {
        try {
            return MAPPER.readValue(json,
                    defaultInstance().constructCollectionType(collectionType, valueType));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <K, V, T extends Map<K, V>> T ofJsonMap(String json, Class<? extends Map> mapType,
            Class<K> keyType, Class<V> valueType) {
        try {
            return MAPPER.readValue(json,
                    defaultInstance().constructMapType(mapType, keyType, valueType));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <K, V, T extends Map<K, Set<V>>> T ofSetMap(String json,
            Class<? extends Map> mapType, Class<K> keyClass, Class<V> subValueClass) {
        JavaType valueType = MAPPER.getTypeFactory()
                .constructCollectionType(Set.class, subValueClass);
        return ofCollectionMap(json, mapType, keyClass, valueType);
    }

    public static <K, V, T extends Map<K, List<V>>> T ofListMap(String json,
            Class<? extends Map> mapType, Class<K> keyClass, Class<V> subValueClass) {
        JavaType valueType = MAPPER.getTypeFactory()
                .constructCollectionType(List.class, subValueClass);
        return ofCollectionMap(json, mapType, keyClass, valueType);
    }

    public static <K, K1, V1, T extends Map<K, Map<K1, V1>>> T ofMapMap(String json,
            Class<? extends Map> mapType, Class<K> keyClass, Class<K1> subKeyClass,
            Class<V1> subValueClass) {
        JavaType valueType = MAPPER.getTypeFactory()
                .constructMapType(Map.class, subKeyClass, subValueClass);
        return ofCollectionMap(json, mapType, keyClass, valueType);
    }

    public static <T> T ofCollectionMap(String json, Class<? extends Map> mapType,
            Class<?> keyClass, JavaType valueType) {
        try {
            JavaType keyType = MAPPER.getTypeFactory().constructType(keyClass);
            return MAPPER.readValue(json,
                    defaultInstance().constructMapType(mapType, keyType, valueType));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <E, T extends Collection<E>> T ofJsonCollection(byte[] bytes,
            Class<? extends Collection> collectionType, Class<E> valueType) {
        try {
            return MAPPER.readValue(bytes,
                    defaultInstance().constructCollectionType(collectionType, valueType));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static <K, V, T extends Map<K, V>> T ofJsonMap(byte[] bytes,
            Class<? extends Map> mapType, Class<K> keyType, Class<V> valueType) {
        try {
            return MAPPER.readValue(bytes,
                    defaultInstance().constructMapType(mapType, keyType, valueType));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean isValidJson(String jsonStr) {
        if (StringUtils.isBlank(jsonStr)) {
            return false;
        }
        try (JsonParser parser = MAPPER.getFactory().createParser(jsonStr)) {
            //noinspection StatementWithEmptyBody
            while (parser.nextToken() != null) {
                // do nothing
            }
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    public static void tryParseJson(String jsonStr) throws IOException {
        if (StringUtils.isBlank(jsonStr)) {
            throw new IOException("json data is empty.");
        }
        try (JsonParser parser = MAPPER.getFactory().createParser(jsonStr)) {
            //noinspection StatementWithEmptyBody
            while (parser.nextToken() != null) {
                // do nothing
            }
        }
    }

    public static Map<String, Object> fromJson(String string) {
        return ofJsonMap(string, Map.class, String.class, Object.class);
    }

    public static Map<String, Object> fromJson(byte[] bytes) {
        return ofJsonMap(bytes, Map.class, String.class, Object.class);
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }

    enum State {
        OUTSIDE_COMMENT,
        INSIDE_LINECOMMENT,
        INSIDE_BLOCKCOMMENT,
        INSIDE_STRING,
    }

    public static String removeComments(String code) {
        State state = State.OUTSIDE_COMMENT;
        StringBuilder result = new StringBuilder();
        int length = code.length();
        int i = 0;
        while (i < length) {
            char c = code.charAt(i);
            char nc = i < length - 1 ? code.charAt(i + 1) : 0;
            i += 1;
            switch (state) {
                case OUTSIDE_COMMENT:
                    if (c == '/' && nc != 0) {
                        i += 1;
                        if (nc == '/') {
                            state = State.INSIDE_LINECOMMENT;
                        } else if (nc == '*') {
                            state = State.INSIDE_BLOCKCOMMENT;
                        } else {
                            result.append(c).append(nc);
                        }
                    } else {
                        result.append(c);
                        if (c == '"') {
                            state = State.INSIDE_STRING;
                        }
                    }
                    break;
                case INSIDE_STRING:
                    result.append(c);
                    if (c == '"') {
                        state = State.OUTSIDE_COMMENT;
                    } else if (c == '\\' && nc != 0) {
                        i += 1;
                        result.append(nc);
                    }
                    break;
                case INSIDE_LINECOMMENT:
                    if (c == '\n' || c == '\r') {
                        state = State.OUTSIDE_COMMENT;
                        result.append(c);
                    }
                    break;
                case INSIDE_BLOCKCOMMENT:
                    if (c == '*' && nc == '/') {
                        i += 1;
                        state = State.OUTSIDE_COMMENT;
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + state);
            }
        }
        String noCommentsJson = result.toString();
        if (code.length() != noCommentsJson.length()) {
            PlatoJsonUtil.checkRemoveComments(noCommentsJson, code);
        }
        return noCommentsJson;
    }

    public static void checkRemoveComments(String noCmtJson, String originJson) {
        try {
            String json1 = mapper().writeValueAsString(mapper().readTree(originJson));
            ObjectMapper disableCmtMapper = mapper().copy().disable(ALLOW_COMMENTS);
            String json2 = disableCmtMapper.writeValueAsString(disableCmtMapper.readTree(noCmtJson));
            if (!json1.equals(json2)) {
                throw new IllegalStateException("remove json comments failed");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}